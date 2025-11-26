package cn.smartjavaai.translation.model;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.inference.Predictor;
import ai.djl.modality.nlp.generate.CausalLMOutput;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.NoopTranslator;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.pool.CommonPredictorFactory;
import cn.smartjavaai.translation.config.TranslationModelConfig;
import cn.smartjavaai.translation.config.NllbSearchConfig;
import cn.smartjavaai.translation.entity.GreedyBatchTensorList;
import cn.smartjavaai.translation.entity.TranslateParam;
import cn.smartjavaai.translation.exception.TranslationException;
import cn.smartjavaai.translation.factory.TranslationModelFactory;
import cn.smartjavaai.translation.model.translator.NllbDecoder2Translator;
import cn.smartjavaai.translation.model.translator.NllbDecoderTranslator;
import cn.smartjavaai.translation.model.translator.NllbEncoderTranslator;
import cn.smartjavaai.translation.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Nllb机器翻译模型
 *
 * @author lwx
 * @date 2025/6/05
 */
@Slf4j
public class NllbModel implements TranslationModel{

    private GenericObjectPool<Predictor<?, ?>> encodePredictorPool;

    private GenericObjectPool<Predictor<?, ?>> decodePredictorPool;

    private GenericObjectPool<Predictor<?, ?>> decode2PredictorPool;

    private ZooModel<NDList, NDList> nllbModel;
    private HuggingFaceTokenizer tokenizer;

    private NllbSearchConfig searchConfig;
    private TranslationModelConfig config;



    @Override
    public void loadModel(TranslationModelConfig config) {
        if (StringUtils.isBlank(config.getModelPath())) {
            throw new TranslationException("modelPath is null");
        }
        Device device = null;
        if (!Objects.isNull(config.getDevice())) {
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        this.config = config;
        Path modelPath = Paths.get(config.getModelPath());
        //this.searchConfig = config.getSearchConfig();
        Criteria<NDList, NDList> criteria =
                Criteria.builder()
                        .setTypes(NDList.class, NDList.class)
                        .optModelPath(modelPath)
                        .optEngine("PyTorch")
                        .optDevice(device)
                        .optTranslator(new NoopTranslator())
                        .build();
        try {
            nllbModel = ModelZoo.loadModel(criteria);
            encodePredictorPool = new GenericObjectPool<>(new CommonPredictorFactory(nllbModel,new NllbEncoderTranslator()));
            decodePredictorPool = new GenericObjectPool<>(new CommonPredictorFactory(nllbModel,new NllbDecoderTranslator()));
            decode2PredictorPool = new GenericObjectPool<>(new CommonPredictorFactory(nllbModel,new NllbDecoder2Translator()));
            Path tokenizerPath = modelPath.getParent().resolve("tokenizer.json");
            tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath);
            //初始化searchConfig
            this.searchConfig = new NllbSearchConfig();
            int predictorPoolSize = config.getPredictorPoolSize();
            if(config.getPredictorPoolSize() <= 0){
                predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
            }
            encodePredictorPool.setMaxTotal(predictorPoolSize);
            decodePredictorPool.setMaxTotal(predictorPoolSize);
            decode2PredictorPool.setMaxTotal(predictorPoolSize);
            log.debug("当前设备: " + nllbModel.getNDManager().getDevice());
            log.debug("当前引擎: " + Engine.getInstance().getEngineName());
            log.debug("模型推理器线程池最大数量: " + predictorPoolSize);
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new TranslationException("模型加载失败", e);
        }
    }

    @Override
    public R<String> translate(TranslateParam translateParam) {
        if(translateParam == null){
            return R.fail(R.Status.PARAM_ERROR);
        }
        //验证
        R<String> validateResult = translateParam.validate();
        if(!validateResult.isSuccess()){
            return validateResult;
        }
        //补充参数
        this.searchConfig.setSrcLangId(translateParam.getSourceLanguage().getId());
        this.searchConfig.setForcedBosTokenId(translateParam.getTargetLanguage().getId());
        return R.ok(translateLanguage(translateParam));
    }

    private String translateLanguage(TranslateParam translateParam) {
        Predictor<long[], NDArray> encoderPredictor = null;
        Predictor<NDList, CausalLMOutput> decoderPredictor = null;
        Predictor<NDList, CausalLMOutput> decoder2Predictor = null;
        try (NDManager manager = NDManager.newBaseManager()) {
            encoderPredictor = (Predictor<long[], NDArray>)encodePredictorPool.borrowObject();
            decoderPredictor = (Predictor<NDList, CausalLMOutput>)decodePredictorPool.borrowObject();
            decoder2Predictor = (Predictor<NDList, CausalLMOutput>)decode2PredictorPool.borrowObject();

            Encoding encoding = tokenizer.encode(translateParam.getInput());
            long[] ids = encoding.getIds();
            // 1. Encoder
            long[] inputIds = new long[ids.length];
            // 设置源语言编码
            inputIds[0] = searchConfig.getSrcLangId();
            for (int i = 0; i < ids.length - 1; i++) {
                inputIds[i + 1] = ids[i];
            }

            long[] attentionMask = encoding.getAttentionMask();
            NDArray attentionMaskArray = manager.create(attentionMask).expandDims(0);

            NDArray encoderHiddenStates = encoderPredictor.predict(inputIds);

            NDArray decoder_input_ids = manager.create(new long[]{searchConfig.getDecoderStartTokenId()}).reshape(1, 1);
            NDList decoderInput = new NDList(decoder_input_ids, encoderHiddenStates, attentionMaskArray);

            // 2. Initial Decoder
            CausalLMOutput modelOutput = decoderPredictor.predict(decoderInput);
            modelOutput.getLogits().attach(manager);
            modelOutput.getPastKeyValuesList().attach(manager);

            GreedyBatchTensorList searchState =
                    new GreedyBatchTensorList(null, decoder_input_ids, modelOutput.getPastKeyValuesList(), encoderHiddenStates, attentionMaskArray);

            while (true) {
//            try (NDScope ignore = new NDScope()) {
                NDArray pastOutputIds = searchState.getPastOutputIds();
                if (searchState.getNextInputIds() != null) {
                    decoderInput = new NDList(searchState.getNextInputIds(), searchState.getEncoderHiddenStates(), searchState.getAttentionMask());
                    decoderInput.addAll(searchState.getPastKeyValues());
                    // 3. Decoder loop
                    modelOutput = decoder2Predictor.predict(decoderInput);
                }
                NDArray outputIds = greedyStepGen(searchConfig, pastOutputIds, modelOutput.getLogits(), manager);

                searchState.setNextInputIds(outputIds);
                pastOutputIds = pastOutputIds.concat(outputIds, 1);
                searchState.setPastOutputIds(pastOutputIds);

                searchState.setPastKeyValues(modelOutput.getPastKeyValuesList());

                long id = searchState.getNextInputIds().toLongArray()[0];
                if (searchConfig.getEosTokenId() == id) {
                    searchState.setNextInputIds(null);
                    break;
                }
                if (searchState.getPastOutputIds() != null && searchState.getPastOutputIds().getShape().get(1) + 1 >= searchConfig.getMaxSeqLength()) {
                    break;
                }
            }

            if (searchState.getNextInputIds() == null) {
                NDArray resultIds = searchState.getPastOutputIds();
                String result = TokenUtils.decode(searchConfig, tokenizer, resultIds);
                return result;
            } else {
                NDArray resultIds = searchState.getPastOutputIds(); // .concat(searchState.getNextInputIds(), 1)
                String result = TokenUtils.decode(searchConfig, tokenizer, resultIds);
                return result;
            }
        } catch (Exception e) {
            throw new TranslationException("翻译错误", e);
        } finally {
            if (encoderPredictor != null) {
                try {
                    encodePredictorPool.returnObject(encoderPredictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        encoderPredictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
            if (decoderPredictor != null) {
                try {
                    decodePredictorPool.returnObject(decoderPredictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        decoderPredictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
            if (decoder2Predictor != null) {
                try {
                    decode2PredictorPool.returnObject(decoder2Predictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        decoder2Predictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
        }
    }

    public NDArray greedyStepGen(NllbSearchConfig config, NDArray pastOutputIds, NDArray next_token_scores, NDManager manager) {
        next_token_scores = next_token_scores.get(":, -1, :");

        NDArray new_next_token_scores = manager.create(next_token_scores.getShape(), next_token_scores.getDataType());
        next_token_scores.copyTo(new_next_token_scores);

        // LogitsProcessor 1. ForcedBOSTokenLogitsProcessor
        // 设置目标语言
        long cur_len = pastOutputIds.getShape().getLastDimension();
        if (cur_len == 1) {
            long num_tokens = new_next_token_scores.getShape().getLastDimension();
            for (long i = 0; i < num_tokens; i++) {
                if (i != config.getForcedBosTokenId()) {
                    new_next_token_scores.set(new NDIndex(":," + i), Float.NEGATIVE_INFINITY);
                }
            }
            new_next_token_scores.set(new NDIndex(":," + config.getForcedBosTokenId()), 0);
        }

        NDArray probs = new_next_token_scores.softmax(-1);
        NDArray next_tokens = probs.argMax(-1);

        return next_tokens.expandDims(0);
    }

    public GenericObjectPool<Predictor<?, ?>> getEncodePredictorPool() {
        return encodePredictorPool;
    }

    public GenericObjectPool<Predictor<?, ?>> getDecodePredictorPool() {
        return decodePredictorPool;
    }

    public GenericObjectPool<Predictor<?, ?>> getDecode2PredictorPool() {
        return decode2PredictorPool;
    }

    @Override
    public void close() throws Exception {
        if (fromFactory) {
            TranslationModelFactory.removeFromCache(config.getModelEnum());
        }
        try {
            if (nllbModel != null) {
                nllbModel.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
        try {
            if (tokenizer != null) {
                tokenizer.close();
            }
        } catch (Exception e) {
            log.warn("关闭 tokenizer 失败", e);
        }
        try {
            if (encodePredictorPool != null) {
                encodePredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 encodePredictorPool 失败", e);
        }
        try {
            if (decodePredictorPool != null) {
                decodePredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 decodePredictorPool 失败", e);
        }
        try {
            if (decode2PredictorPool != null) {
                decode2PredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 decode2PredictorPool 失败", e);
        }
    }

    private boolean fromFactory = false;

    @Override
    public void setFromFactory(boolean fromFactory) {
        this.fromFactory = fromFactory;
    }
    public boolean isFromFactory() {
        return fromFactory;
    }
}
