package cn.smartjavaai.translation.model;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.nlp.generate.CausalLMOutput;
import ai.djl.modality.nlp.generate.SearchConfig;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDArrays;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.sentencepiece.SpTokenizer;
import ai.djl.translate.NoopTranslator;
import ai.djl.translate.TranslateException;
import ai.djl.util.Utils;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.pool.CommonPredictorFactory;
import cn.smartjavaai.translation.config.NllbSearchConfig;
import cn.smartjavaai.translation.config.OpusSearchConfig;
import cn.smartjavaai.translation.config.TranslationModelConfig;
import cn.smartjavaai.translation.entity.BeamBatchTensorList;
import cn.smartjavaai.translation.entity.GreedyBatchTensorList;
import cn.smartjavaai.translation.entity.TranslateParam;
import cn.smartjavaai.translation.exception.TranslationException;
import cn.smartjavaai.translation.factory.TranslationModelFactory;
import cn.smartjavaai.translation.model.translator.NllbDecoder2Translator;
import cn.smartjavaai.translation.model.translator.NllbDecoderTranslator;
import cn.smartjavaai.translation.model.translator.NllbEncoderTranslator;
import cn.smartjavaai.translation.model.translator.opus.Decoder2Translator;
import cn.smartjavaai.translation.model.translator.opus.DecoderTranslator;
import cn.smartjavaai.translation.model.translator.opus.EncoderTranslator;
import cn.smartjavaai.translation.utils.NDArrayUtils;
import cn.smartjavaai.translation.utils.TokenUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpusMt机器翻译模型
 *
 * @author dwj
 */
@Slf4j
public class OpusMtModel implements TranslationModel{

    private GenericObjectPool<Predictor<?, ?>> encodePredictorPool;

    private GenericObjectPool<Predictor<?, ?>> decodePredictorPool;

    private GenericObjectPool<Predictor<?, ?>> decode2PredictorPool;

    private ZooModel<NDList, NDList> model;
    private SpTokenizer sourceTokenizer;

    private OpusSearchConfig searchConfig;
    private TranslationModelConfig config;

    private ConcurrentHashMap<String, Long> map;

    private ConcurrentHashMap<Long, String> reverseMap;

    private float length_penalty = 1.0f;
    private boolean do_early_stopping = false;
    private int num_beam_hyps_to_keep = 1;
    private int num_beam_groups = 1;



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
        Criteria<NDList, NDList> criteria =
                Criteria.builder()
                        .setTypes(NDList.class, NDList.class)
                        .optModelPath(modelPath)
                        .optEngine("PyTorch")
                        .optDevice(device)
                        .optTranslator(new NoopTranslator())
                        .build();
        try {
            model = ModelZoo.loadModel(criteria);
            encodePredictorPool = new GenericObjectPool<>(new CommonPredictorFactory(model,new EncoderTranslator()));
            decodePredictorPool = new GenericObjectPool<>(new CommonPredictorFactory(model,new DecoderTranslator()));
            decode2PredictorPool = new GenericObjectPool<>(new CommonPredictorFactory(model,new Decoder2Translator()));

            Path tokenizerPath = modelPath.getParent().resolve("source.spm");
            sourceTokenizer = new SpTokenizer(tokenizerPath);
            List<String> words = Utils.readLines(modelPath.getParent().resolve("vocab.txt"));
            String jsonStr = "";
            for (String line : words) {
                jsonStr = jsonStr + line;
            }
            map = new Gson().fromJson(jsonStr, new TypeToken<ConcurrentHashMap<String, Long>>() {
            }.getType());
            reverseMap = new ConcurrentHashMap<>();
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Long> next = (Map.Entry<String, Long>) it.next();
                reverseMap.put(next.getValue(), next.getKey());
            }
            //初始化searchConfig
            this.searchConfig = new OpusSearchConfig();
            int predictorPoolSize = config.getPredictorPoolSize();
            if(config.getPredictorPoolSize() <= 0){
                predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
            }
            encodePredictorPool.setMaxTotal(predictorPoolSize);
            decodePredictorPool.setMaxTotal(predictorPoolSize);
            decode2PredictorPool.setMaxTotal(predictorPoolSize);
            log.debug("当前设备: " + model.getNDManager().getDevice());
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
        if (StringUtils.isBlank(translateParam.getInput())) {
            return R.fail(R.Status.PARAM_ERROR.getCode(), "输入文本不能为空");
        }
        return R.ok(translateLanguage(translateParam));
    }

    @Override
    public R<String> translate(String input) {
        //验证
        if (StringUtils.isBlank(input)) {
            return R.fail(R.Status.PARAM_ERROR.getCode(), "输入文本不能为空");
        }
        return R.ok(translateLanguage(new TranslateParam(input)));
    }

    private String translateLanguage(TranslateParam translateParam) {
        try (NDManager manager = NDManager.newBaseManager()) {
            long numBeam = searchConfig.getBeam();
            BeamSearchScorer beamSearchScorer = new BeamSearchScorer((int) numBeam, length_penalty, do_early_stopping, num_beam_hyps_to_keep, num_beam_groups);
            // 1. Encode
            List<String> tokens = sourceTokenizer.tokenize(translateParam.getInput());
            String[] strs = tokens.toArray(new String[]{});
            log.info("Tokens: " + Arrays.toString(strs));
            int[] sourceIds = new int[tokens.size() + 1];
            sourceIds[tokens.size()] = 0;
            for (int i = 0; i < tokens.size(); i++) {
                sourceIds[i] = map.get(tokens.get(i)).intValue();
            }
            NDArray encoder_hidden_states = encoder(sourceIds);
            encoder_hidden_states = NDArrayUtils.expand(encoder_hidden_states, searchConfig.getBeam());

            NDArray decoder_input_ids = manager.create(new long[]{65000}).reshape(1, 1);
            decoder_input_ids = NDArrayUtils.expand(decoder_input_ids, numBeam);


            long[] attentionMask = new long[sourceIds.length];
            Arrays.fill(attentionMask, 1);
            NDArray attentionMaskArray = manager.create(attentionMask).expandDims(0);
            NDArray new_attention_mask = NDArrayUtils.expand(attentionMaskArray, searchConfig.getBeam());
            NDList decoderInput = new NDList(decoder_input_ids, encoder_hidden_states, new_attention_mask);


            // 2. Initial Decoder
            CausalLMOutput modelOutput = decoder(decoderInput);
            modelOutput.getLogits().attach(manager);
            modelOutput.getPastKeyValuesList().attach(manager);

            NDArray beam_scores = manager.zeros(new Shape(1, numBeam), DataType.FLOAT32);
            beam_scores.set(new NDIndex(":, 1:"), -1e9);
            beam_scores = beam_scores.reshape(numBeam, 1);

            NDArray input_ids = decoder_input_ids;
            BeamBatchTensorList searchState = new BeamBatchTensorList(null, new_attention_mask, encoder_hidden_states, modelOutput.getPastKeyValuesList());
            NDArray next_tokens;
            NDArray next_indices;
            while (true) {
                if (searchState.getNextInputIds() != null) {
                    decoder_input_ids = searchState.getNextInputIds().get(new NDIndex(":, -1:"));
                    decoderInput = new NDList(decoder_input_ids, searchState.getEncoderHiddenStates(), searchState.getAttentionMask());
                    decoderInput.addAll(searchState.getPastKeyValues());
                    // 3. Decoder loop
                    modelOutput = decoder2(decoderInput);
                }

                NDArray next_token_logits = modelOutput.getLogits().get(":, -1, :");

                // hack: adjust tokens for Marian. For Marian we have to make sure that the `pad_token_id`
                // cannot be generated both before and after the `nn.functional.log_softmax` operation.
                NDArray new_next_token_logits = manager.create(next_token_logits.getShape(), next_token_logits.getDataType());
                next_token_logits.copyTo(new_next_token_logits);
                new_next_token_logits.set(new NDIndex(":," + searchConfig.getPadTokenId()), Float.NEGATIVE_INFINITY);

                NDArray next_token_scores = new_next_token_logits.logSoftmax(1);

                // next_token_scores = logits_processor(input_ids, next_token_scores)
                // 1. NoBadWordsLogitsProcessor
                next_token_scores.set(new NDIndex(":," + searchConfig.getPadTokenId()), Float.NEGATIVE_INFINITY);

                // 2. MinLengthLogitsProcessor 没生效
                // 3. ForcedEOSTokenLogitsProcessor
                long cur_len = input_ids.getShape().getLastDimension();
                if (cur_len == (searchConfig.getMaxSeqLength() - 1)) {
                    long num_tokens = next_token_scores.getShape().getLastDimension();
                    for (long i = 0; i < num_tokens; i++) {
                        if(i != searchConfig.getEosTokenId()){
                            next_token_scores.set(new NDIndex(":," + i), Float.NEGATIVE_INFINITY);
                        }
                    }
                    next_token_scores.set(new NDIndex(":," + searchConfig.getEosTokenId()), 0);
                }

                long vocab_size = next_token_scores.getShape().getLastDimension();
                beam_scores = beam_scores.repeat(1, vocab_size);
                next_token_scores = next_token_scores.add(beam_scores);

                // reshape for beam search
                next_token_scores = next_token_scores.reshape(1, numBeam * vocab_size);

                // [batch, beam]
                NDList topK = next_token_scores.topK(Math.toIntExact(numBeam) * 2, 1, true, true);

                next_token_scores = topK.get(0);
                next_tokens = topK.get(1);

                // next_indices = next_tokens // vocab_size
                next_indices = next_tokens.div(vocab_size).toType(DataType.INT64, true);

                // next_tokens = next_tokens % vocab_size
                next_tokens = next_tokens.mod(vocab_size);

                // stateless
                NDList beam_outputs = beamSearchScorer.process(manager, input_ids, next_token_scores, next_tokens, next_indices, searchConfig.getPadTokenId(), searchConfig.getEosTokenId());

                beam_scores = beam_outputs.get(0).reshape(numBeam, 1);
                NDArray beam_next_tokens = beam_outputs.get(1);
                NDArray beam_idx = beam_outputs.get(2);

                // input_ids = torch.cat([input_ids[beam_idx, :], beam_next_tokens.unsqueeze(-1)], dim=-1)
                long[] beam_next_tokens_arr = beam_next_tokens.toLongArray();
                long[] beam_idx_arr = beam_idx.toLongArray();
                NDList inputList = new NDList();
                for (int i = 0; i < numBeam; i++) {
                    long index = beam_idx_arr[i];
                    NDArray ndArray = input_ids.get(index).reshape(1, input_ids.getShape().getLastDimension());
                    ndArray = ndArray.concat(manager.create(beam_next_tokens_arr[i]).reshape(1, 1), 1);
                    inputList.add(ndArray);
                }
                input_ids = NDArrays.concat(inputList, 0);
                searchState.setNextInputIds(input_ids);
                searchState.setPastKeyValues(modelOutput.getPastKeyValuesList());

                boolean maxLengthCriteria = (input_ids.getShape().getLastDimension() >= searchConfig.getMaxSeqLength());
                if (beamSearchScorer.isDone() || maxLengthCriteria) {
                    break;
                }

            }

            long[] sequences = beamSearchScorer.finalize(searchConfig.getMaxSeqLength(), searchConfig.getEosTokenId());
            String result = TokenUtils.decode(reverseMap, sequences);
            return result;
        } catch (Exception e) {
            throw new TranslationException("翻译错误", e);
        }
    }

    public NDArray encoder(int[] ids) {
        Predictor<int[], NDArray> predictor = null;
        try {
            predictor = (Predictor<int[], NDArray>)encodePredictorPool.borrowObject();
            return predictor.predict(ids);
        } catch (Exception e) {
            throw new TranslationException("机器翻译编码错误", e);
        }finally {
            if (predictor != null) {
                try {
                    encodePredictorPool.returnObject(predictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        predictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
        }
    }

    public CausalLMOutput decoder(NDList input) throws TranslateException {
        Predictor<NDList, CausalLMOutput> predictor = null;
        try {
            predictor = (Predictor<NDList, CausalLMOutput>)decodePredictorPool.borrowObject();
            return predictor.predict(input);
        } catch (Exception e) {
            throw new TranslationException("机器翻译编码错误", e);
        }finally {
            if (predictor != null) {
                try {
                    decodePredictorPool.returnObject(predictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        predictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
        }
    }

    public CausalLMOutput decoder2(NDList input) throws TranslateException {
        Predictor<NDList, CausalLMOutput> predictor = null;
        try {
            predictor = (Predictor<NDList, CausalLMOutput>)decode2PredictorPool.borrowObject();
            return predictor.predict(input);
        } catch (Exception e) {
            throw new TranslationException("机器翻译编码错误", e);
        }finally {
            if (predictor != null) {
                try {
                    decode2PredictorPool.returnObject(predictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        predictor.close(); // 归还失败才销毁
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
            if (model != null) {
                model.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
        try {
            if (sourceTokenizer != null) {
                sourceTokenizer.close();
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
