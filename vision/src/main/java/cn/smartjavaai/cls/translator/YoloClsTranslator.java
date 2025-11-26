package cn.smartjavaai.cls.translator;

import ai.djl.Model;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.*;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.translate.*;
import ai.djl.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class YoloClsTranslator implements Translator<Image, Classifications> {


    protected float threshold;
    protected List<String> classes;
    protected boolean applyRatio;
    protected Pipeline pipeline;
    private Image.Flag flag;
    private Batchifier batchifier;
    protected int width;
    protected int height;
    protected int topk;

    private SynsetLoader synsetLoader;

    @Override
    public void prepare(TranslatorContext ctx) throws IOException {
        if (this.classes == null) {
            this.classes = this.synsetLoader.load(ctx.getModel());
        }
    }


    /**
     * Constructs an ImageTranslator with the provided builder.
     *
     * @param builder the data to build with
     */
    protected YoloClsTranslator(Builder builder) {
        this.threshold = builder.threshold;
        this.synsetLoader = builder.synsetLoader;
        this.applyRatio = builder.applyRatio;
        this.flag = builder.flag;
        this.pipeline = builder.pipeline;
        this.batchifier = builder.batchifier;
        this.width = builder.width;
        this.height = builder.height;
    }

    /**
     * Creates a builder to build a {@code YoloV8Translator} with specified arguments.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder to build a {@code YoloV8Translator} with specified arguments.
     *
     * @param arguments arguments to specify builder options
     * @return a new builder
     */
    public static Builder builder(Map<String, ?> arguments) {
        Builder builder = new Builder();
        builder.configPreProcess(arguments);
        builder.configPostProcess(arguments);
        return builder;
    }

    @Override
    public NDList processInput(TranslatorContext ctx, Image input) throws Exception {
        NDManager manager = ctx.getNDManager();
        NDArray array = input.toNDArray(manager, Image.Flag.COLOR);
        //中心裁剪
        array = NDImageUtils.centerCrop(array);
        array = NDImageUtils.resize(array, width, height);
        // 转为 float32 且归一化到 0~1
        array = array.toType(DataType.FLOAT32, false).div(255f); // HWC
        // HWC -> CHW
        array = array.transpose(2, 0, 1); // CHW
        return new NDList(array);
    }

    @Override
    public Classifications processOutput(TranslatorContext ctx, NDList list) throws Exception {
        NDArray probabilitiesNd = list.singletonOrThrow();
//        probabilitiesNd = probabilitiesNd.softmax(0);
        return new Classifications(classes, probabilitiesNd, 5);
    }



    public static class Builder {


        protected float threshold = 0.2F;
        protected boolean applyRatio;
        protected boolean removePadding;

        protected int width = 224;
        protected int height = 224;
        protected Image.Flag flag;
        protected Pipeline pipeline;
        protected Batchifier batchifier;
        protected int topk = 5;

        protected SynsetLoader synsetLoader;

        public Builder() {
        }

        /**
         * Builds the translator.
         *
         * @return the new translator
         */
        public YoloClsTranslator build() {
            if (pipeline == null) {
                addTransform(
                        array -> array.transpose(2, 0, 1).toType(DataType.FLOAT32, false).div(255));
            }
//            validate();
            return new YoloClsTranslator(this);
        }

        protected Builder self() {
            return this;
        }

        public Builder addTransform(Transform transform) {
            if (this.pipeline == null) {
                this.pipeline = new Pipeline();
            }

            this.pipeline.add(transform);
            return this.self();
        }

        public Builder optApplyRatio(boolean value) {
            this.applyRatio = value;
            return this.self();
        }

        public Builder optFlag(Image.Flag flag) {
            this.flag = flag;
            return this.self();
        }

        public Builder setPipeline(Pipeline pipeline) {
            this.pipeline = pipeline;
            return this.self();
        }

        public Builder setImageSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this.self();
        }


        public Builder optBatchifier(Batchifier batchifier) {
            this.batchifier = batchifier;
            return this.self();
        }

        public Builder optThreshold(float threshold) {
            this.threshold = threshold;
            return this.self();
        }

        public Builder optTopk(int topk) {
            this.topk = topk;
            return this.self();
        }

        /**
         * Sets the name of the synset file listing the potential classes for an image.
         *
         * @param synsetArtifactName a file listing the potential classes for an image
         * @return the builder
         */
        public Builder optSynsetArtifactName(String synsetArtifactName) {
            synsetLoader = new SynsetLoader(synsetArtifactName);
            return self();
        }

        /**
         * Sets the URL of the synset file.
         *
         * @param synsetUrl the URL of the synset file
         * @return the builder
         */
        public Builder optSynsetUrl(String synsetUrl) {
            try {
                this.synsetLoader = new SynsetLoader(new URL(synsetUrl));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Invalid synsetUrl: " + synsetUrl, e);
            }
            return self();
        }

        /**
         * Sets the potential classes for an image.
         *
         * @param synset the potential classes for an image
         * @return the builder
         */
        public Builder optSynset(List<String> synset) {
            synsetLoader = new SynsetLoader(synset);
            return self();
        }

        /** {@inheritDoc} */
        protected void configPostProcess(Map<String, ?> arguments) {
            if (ArgumentsUtil.booleanValue(arguments, "optApplyRatio") || ArgumentsUtil.booleanValue(arguments, "applyRatio")) {
                this.optApplyRatio(true);
            }
            this.threshold = ArgumentsUtil.floatValue(arguments, "threshold", 0.2F);
            String centerFit = ArgumentsUtil.stringValue(arguments, "centerFit", "false");
            this.removePadding = "true".equals(centerFit);
            String type = ArgumentsUtil.stringValue(arguments, "outputType", "AUTO");
        }

        protected void configPreProcess(Map<String, ?> arguments) {
            if (this.pipeline == null) {
                this.pipeline = new Pipeline();
            }

            this.width = ArgumentsUtil.intValue(arguments, "width", 224);
            this.height = ArgumentsUtil.intValue(arguments, "height", 224);
            if (arguments.containsKey("flag")) {
                this.flag = Image.Flag.valueOf(arguments.get("flag").toString());
            }

            String pad = ArgumentsUtil.stringValue(arguments, "pad", "false");
            if ("true".equals(pad)) {
                this.addTransform(new Pad(0.0));
            } else if (!"false".equals(pad)) {
                double padding = Double.parseDouble(pad);
                this.addTransform(new Pad(padding));
            }

            String resize = ArgumentsUtil.stringValue(arguments, "resize", "false");
            int w;
            int shortEdge;
            if ("true".equals(resize)) {
                this.addTransform(new Resize(this.width, this.height));
            } else if (!"false".equals(resize)) {
                String[] tokens = resize.split("\\s*,\\s*");
                w = (int)Double.parseDouble(tokens[0]);
                if (tokens.length > 1) {
                    shortEdge = (int)Double.parseDouble(tokens[1]);
                } else {
                    shortEdge = w;
                }

                Image.Interpolation interpolation;
                if (tokens.length > 2) {
                    interpolation = Image.Interpolation.valueOf(tokens[2]);
                } else {
                    interpolation = Image.Interpolation.BILINEAR;
                }

                this.addTransform(new Resize(w, shortEdge, interpolation));
            }

            String resizeShort = ArgumentsUtil.stringValue(arguments, "resizeShort", "false");
            if ("true".equals(resizeShort)) {
                w = Math.max(this.width, this.height);
                this.addTransform(new ResizeShort(w));
            } else if (!"false".equals(resizeShort)) {
                String[] tokens = resizeShort.split("\\s*,\\s*");
                shortEdge = (int)Double.parseDouble(tokens[0]);
                int longEdge;
                if (tokens.length > 1) {
                    longEdge = (int)Double.parseDouble(tokens[1]);
                } else {
                    longEdge = -1;
                }

                Image.Interpolation interpolation;
                if (tokens.length > 2) {
                    interpolation = Image.Interpolation.valueOf(tokens[2]);
                } else {
                    interpolation = Image.Interpolation.BILINEAR;
                }

                this.addTransform(new ResizeShort(shortEdge, longEdge, interpolation));
            }

            if (ArgumentsUtil.booleanValue(arguments, "centerCrop", false)) {
                this.addTransform(new CenterCrop(this.width, this.height));
            }

            if (ArgumentsUtil.booleanValue(arguments, "centerFit")) {
                this.addTransform(new CenterFit(this.width, this.height));
            }

            if (ArgumentsUtil.booleanValue(arguments, "toTensor", true)) {
                this.addTransform(new ToTensor());
            }

            String normalize = ArgumentsUtil.stringValue(arguments, "normalize", "false");
            if ("true".equals(normalize)) {
                float[] MEAN = new float[]{0.485F, 0.456F, 0.406F};
                float[] STD = new float[]{0.229F, 0.224F, 0.225F};
                this.addTransform(new Normalize(MEAN, STD));
            } else if (!"false".equals(normalize)) {
                String[] tokens = normalize.split("\\s*,\\s*");
                if (tokens.length != 6) {
                    throw new IllegalArgumentException("Invalid normalize value: " + normalize);
                }

                float[] mean = new float[]{Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2])};
                float[] std = new float[]{Float.parseFloat(tokens[3]), Float.parseFloat(tokens[4]), Float.parseFloat(tokens[5])};
                this.addTransform(new Normalize(mean, std));
            }

            String range = (String)arguments.get("range");
            if ("0,1".equals(range)) {
                this.addTransform((a) -> {
                    return a.div(255.0F);
                });
            } else if ("-1,1".equals(range)) {
                this.addTransform((a) -> {
                    return a.div(128.0F).sub(1);
                });
            }

            if (arguments.containsKey("batchifier")) {
                this.batchifier = Batchifier.fromString((String)arguments.get("batchifier"));
            }

        }
    }



    protected static final class SynsetLoader {

        private String synsetFileName;
        private URL synsetUrl;
        private List<String> synset;

        public SynsetLoader(List<String> synset) {
            this.synset = synset;
        }

        public SynsetLoader(URL synsetUrl) {
            this.synsetUrl = synsetUrl;
        }

        public SynsetLoader(String synsetFileName) {
            this.synsetFileName = synsetFileName;
        }

        public List<String> load(Model model) throws IOException {
            if (synset != null) {
                return synset;
            } else if (synsetUrl != null) {
                try (InputStream is = synsetUrl.openStream()) {
                    return Utils.readLines(is);
                }
            }
            return model.getArtifact(synsetFileName, Utils::readLines);
        }
    }



}
