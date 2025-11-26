package cn.smartjavaai.obb.translator;

import ai.djl.Model;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.*;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.*;
import ai.djl.util.Utils;
import cn.smartjavaai.common.utils.LetterBoxUtils;
import cn.smartjavaai.obb.entity.ObbResult;
import cn.smartjavaai.obb.entity.YoloRotatedBox;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


/**
 * YoloV11OddTranslator
 * @author dwj
 */
public class YoloV11OddTranslator implements Translator<Image, ObbResult> {

    private int maxBoxes;

    private YoloOutputType yoloOutputLayerType;
    private float nmsThreshold;

    protected float threshold;

    protected List<String> classes;
    protected boolean applyRatio;
    protected boolean removePadding;

    protected Pipeline pipeline;
    private Image.Flag flag;
    private Batchifier batchifier;

    protected int width;
    protected int height;

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
    protected YoloV11OddTranslator(Builder builder) {
        this.yoloOutputLayerType = builder.outputType;
        this.nmsThreshold = builder.nmsThreshold;
        maxBoxes = builder.maxBox;
        this.threshold = builder.threshold;
        this.synsetLoader = builder.synsetLoader;
        this.applyRatio = builder.applyRatio;
        this.removePadding = builder.removePadding;
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
    public NDList processInput(TranslatorContext ctx, Image input) {
        NDManager manager = ctx.getNDManager();
        NDArray array = input.toNDArray(manager, Image.Flag.COLOR);
        int imageWidth = (int) array.getShape().get(1);
        int imageHeight = (int) array.getShape().get(0);
        //Letter box resize 640x640 with padding (保持比例，补边缘)
        LetterBoxUtils.ResizeResult letterBoxResult = LetterBoxUtils.letterbox(manager, array, width, height, 114f, LetterBoxUtils.PaddingPosition.CENTER);
        array = letterBoxResult.image;
        // 转为 float32 且归一化到 0~1
        array = array.toType(DataType.FLOAT32, false).div(255f); // HWC
        // HWC -> CHW
        array = array.transpose(2, 0, 1); // CHW

        ctx.setAttachment("width", input.getWidth());
        ctx.setAttachment("height", input.getHeight());
        ctx.setAttachment("processedWidth", width);
        ctx.setAttachment("processedHeight", height);
        return new NDList(array);
    }


    /** {@inheritDoc} */
    protected ObbResult processFromBoxOutput(int imageWidth, int imageHeight, int processedWidth, int processedHeight, NDList list) {


        float scale = Math.min((float) processedWidth / imageWidth, (float) processedHeight / imageHeight);
        float padW = (processedWidth - imageWidth * scale) / 2;
        float padH = (processedHeight - imageHeight * scale) / 2;
        //[cx,cy,w,h,class*15,rotated]
        NDArray rawResult = list.get(0);
        NDArray reshapedResult = rawResult.transpose();
        Shape shape = reshapedResult.getShape();
        float[] buf = reshapedResult.toFloatArray();
        int numberRows = Math.toIntExact(shape.get(0));
        int nClasses = Math.toIntExact(shape.get(1));

        // reverse order search in heap; searches through #maxBoxes for optimization when set
        List<YoloRotatedBox> rotatedBoxes = new ArrayList<>();
        for (int i = numberRows - 1; i > numberRows - maxBoxes; --i) {
            int index = i * nClasses;

            // 找最大类别
            float maxClassProb = -1f;
            int maxIndex = -1;
            for (int c = 4; c < nClasses - 1; c++) { // 类别从4开始，-1是rotated
                float classProb = buf[index + c];
                if (classProb > maxClassProb) {
                    maxClassProb = classProb;
                    maxIndex = c - 4;
                }
            }

            if (maxClassProb > threshold) {
                float cx = buf[index];
                float cy = buf[index + 1];
                float w = buf[index + 2];
                float h = buf[index + 3];

                cx = (cx - padW) / scale;
                cy = (cy - padH) / scale;
                w = w / scale;
                h = h / scale;
                float angle = buf[index + nClasses - 1]; // 最后一个是旋转角度
                YoloRotatedBox rotatedBox = new YoloRotatedBox(cx, cy, w, h, angle, classes.get(maxIndex), maxClassProb);
                rotatedBoxes.add(rotatedBox);
            }
        }
        List<YoloRotatedBox> rotatedBoxeList = rotatedNMS(rotatedBoxes, nmsThreshold);
        return new ObbResult(rotatedBoxeList);
    }


    public static List<YoloRotatedBox> rotatedNMS(List<YoloRotatedBox> boxes, double iouThreshold) {
        List<YoloRotatedBox> keep = new ArrayList<>();
        boolean[] removed = new boolean[boxes.size()];

        // 按 score 降序
//        boxes.sort((b1, b2) -> Float.compare(b2.score, b1.score));

        for (int i = 0; i < boxes.size(); i++) {
            if (removed[i]) continue;
            YoloRotatedBox ibox = boxes.get(i);
            keep.add(ibox);

            for (int j = i + 1; j < boxes.size(); j++) {
                if (removed[j]) continue;
                YoloRotatedBox jbox = boxes.get(j);

                if (!ibox.className.equals(jbox.className)) continue;

                double iou = YoloRotatedBox.probiou(ibox, jbox, 1e-7);
                if (iou > iouThreshold - 1e-7) {
                    removed[j] = true;
                }
            }
        }
        return keep;
    }

    private ObbResult processFromDetectOutput() {
        throw new UnsupportedOperationException(
                "detect layer output is not supported yet, check correct YoloV5 export format");
    }

    @Override
    public ObbResult processOutput(TranslatorContext ctx, NDList list) throws Exception {
        int imageWidth = (Integer) ctx.getAttachment("width");
        int imageHeight = (Integer) ctx.getAttachment("height");
        int processedWidth = (Integer) ctx.getAttachment("processedWidth");
        int processedHeight = (Integer) ctx.getAttachment("processedHeight");

        switch (yoloOutputLayerType) {
            case DETECT:
                return processFromDetectOutput();
            case AUTO:
                if (list.get(0).getShape().dimension() > 2) {
                    return processFromDetectOutput();
                } else {
                    return processFromBoxOutput(imageWidth, imageHeight, processedWidth, processedHeight, list);
                }
            case BOX:
            default:
                return processFromBoxOutput(imageWidth, imageHeight, processedWidth, processedHeight, list);
        }
    }



    public static class Builder {

        private int maxBox = 8400;

        YoloOutputType outputType;
        float nmsThreshold;

        protected float threshold = 0.25F;
        protected boolean applyRatio;
        protected boolean removePadding;

        protected int width = 224;
        protected int height = 224;
        protected Image.Flag flag;
        protected Pipeline pipeline;
        protected Batchifier batchifier;

        protected SynsetLoader synsetLoader;

        public Builder() {
            this.outputType = YoloOutputType.AUTO;
            this.nmsThreshold = 0.45F;
        }

        public Builder optOutputType(YoloOutputType outputType) {
            this.outputType = outputType;
            return this;
        }

        public Builder optNmsThreshold(float nmsThreshold) {
            this.nmsThreshold = nmsThreshold;
            return this;
        }

        /**
         * Builds the translator.
         *
         * @return the new translator
         */
        public YoloV11OddTranslator build() {
            if (pipeline == null) {
                addTransform(
                        array -> array.transpose(2, 0, 1).toType(DataType.FLOAT32, false).div(255));
            }
//            validate();
            return new YoloV11OddTranslator(this);
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
            this.threshold = ArgumentsUtil.floatValue(arguments, "threshold", 0.25F);
            String centerFit = ArgumentsUtil.stringValue(arguments, "centerFit", "false");
            this.removePadding = "true".equals(centerFit);
            String type = ArgumentsUtil.stringValue(arguments, "outputType", "AUTO");
            this.outputType = YoloOutputType.valueOf(type.toUpperCase(Locale.ENGLISH));
            this.nmsThreshold = ArgumentsUtil.floatValue(arguments, "nmsThreshold", 0.45F);
            maxBox = ArgumentsUtil.intValue(arguments, "maxBox", 8400);
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

    public static enum YoloOutputType {
        BOX,
        DETECT,
        AUTO;

        private YoloOutputType() {
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
