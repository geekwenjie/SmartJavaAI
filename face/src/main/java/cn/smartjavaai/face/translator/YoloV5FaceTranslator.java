package cn.smartjavaai.face.translator;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.*;
import ai.djl.modality.cv.transform.*;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.*;
import cn.smartjavaai.common.utils.LetterBoxUtils;

import java.util.*;


/**
 * YoloV5Translator
 * @author dwj
 */
public class YoloV5FaceTranslator implements Translator<Image, DetectedObjects> {

    private int maxBoxes;

    private YoloOutputType yoloOutputLayerType;
    private float nmsThreshold;

    protected float threshold;
//    private BaseImageTranslator.SynsetLoader synsetLoader;
    protected List<String> classes;
    protected boolean applyRatio;
    protected boolean removePadding;

    protected Pipeline pipeline;
    private Image.Flag flag;
    private Batchifier batchifier;
    protected int width;
    protected int height;


    /**
     * Constructs an ImageTranslator with the provided builder.
     *
     * @param builder the data to build with
     */
    protected YoloV5FaceTranslator(Builder builder) {
        this.yoloOutputLayerType = builder.outputType;
        this.nmsThreshold = builder.nmsThreshold;
        maxBoxes = builder.maxBox;
        this.threshold = builder.threshold;
//        this.synsetLoader = builder.synsetLoader;
        this.applyRatio = builder.applyRatio;
        this.removePadding = builder.removePadding;
        this.flag = builder.flag;
        this.pipeline = builder.pipeline;
        this.batchifier = builder.batchifier;
        this.width = builder.width;
        this.height = builder.height;
        classes = Arrays.asList("face");
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
        NDArray array = input.toNDArray(ctx.getNDManager(), flag);
//        NDList list = pipeline.transform(new NDList(array));
//        Shape shape = list.get(0).getShape();
//        int processedWidth;
//        int processedHeight;
//        long[] dim = shape.getShape();
//        if (NDImageUtils.isCHW(shape)) {
//            processedWidth = (int) dim[dim.length - 1];
//            processedHeight = (int) dim[dim.length - 2];
//        } else {
//            processedWidth = (int) dim[dim.length - 2];
//            processedHeight = (int) dim[dim.length - 3];
//        }

        LetterBoxUtils.ResizeResult letterBoxResult = LetterBoxUtils.letterbox(ctx.getNDManager(), array, width, height, 114f, LetterBoxUtils.PaddingPosition.CENTER);
        array = letterBoxResult.image;
        ctx.setAttachment("width", input.getWidth());
        ctx.setAttachment("height", input.getHeight());
        ctx.setAttachment("processedWidth", width);
        ctx.setAttachment("processedHeight", height);
        ctx.setAttachment("scale", letterBoxResult.r);
        // 转为 float32 且归一化到 0~1
        array = array.toType(DataType.FLOAT32, false).div(255f); // HWC
        // HWC -> CHW
        array = array.transpose(2, 0, 1); // CHW
//        return new NDList(array.expandDims(0));
        return new NDList(array);
    }

    @Override
    public DetectedObjects processOutput(TranslatorContext ctx, NDList list) throws Exception {
        int imageWidth = (Integer) ctx.getAttachment("width");
        int imageHeight = (Integer) ctx.getAttachment("height");
        float scale = (Float) ctx.getAttachment("scale");
        switch (yoloOutputLayerType) {
            case DETECT:
                return processFromDetectOutput();
            case AUTO:
                if (list.get(0).getShape().dimension() > 2) {
                    return processFromDetectOutput();
                } else {
                    return processFromBoxOutput(imageWidth, imageHeight, list, scale);
                }
            case BOX:
            default:
                return processFromBoxOutput(imageWidth, imageHeight, list, scale);
        }
    }

    /** {@inheritDoc} */
    protected DetectedObjects processFromBoxOutput(int imageWidth, int imageHeight, NDList list, float scale) {
        float[] flattened = list.get(0).toFloatArray();
        int sizeClasses = classes.size();
        int stride = 15 + sizeClasses;
        int size = flattened.length / stride;

        ArrayList<Landmark> boxes = new ArrayList<>();
        ArrayList<Float> scores = new ArrayList<>();
        ArrayList<Integer> classIds = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            int indexBase = i * stride;
            float maxClass = 0;
            int maxIndex = 0;
//            for (int c = 0; c < sizeClasses; c++) {
//                if (flattened[indexBase + c + 5] > maxClass) {
//                    maxClass = flattened[indexBase + c + 5];
//                    maxIndex = c;
//                }
//            }
            float score = flattened[indexBase + 4];
            if (score > threshold) {
                float xPos = flattened[indexBase];
                float yPos = flattened[indexBase + 1];
                float w = flattened[indexBase + 2];
                float h = flattened[indexBase + 3];
                List<Point> keypoints = new ArrayList<>();
                keypoints.add(new Point(flattened[indexBase + 5], flattened[indexBase + 6]));
                keypoints.add(new Point(flattened[indexBase + 7], flattened[indexBase + 8]));
                keypoints.add(new Point(flattened[indexBase + 9], flattened[indexBase + 10]));
                keypoints.add(new Point(flattened[indexBase + 11], flattened[indexBase + 12]));
                keypoints.add(new Point(flattened[indexBase + 13], flattened[indexBase + 14]));
                Landmark rect =
                        new Landmark(Math.max(0, xPos - w / 2), Math.max(0, yPos - h / 2), w, h,keypoints);
                boxes.add(rect);
                scores.add(score);
                classIds.add(maxIndex);
            }
        }
        return nms(imageWidth, imageHeight, boxes, classIds, scores, scale);
    }

    private DetectedObjects processFromDetectOutput() {
        throw new UnsupportedOperationException(
                "detect layer output is not supported yet, check correct YoloV5 export format");
    }





    protected DetectedObjects nms(
            int imageWidth,
            int imageHeight,
            List<Landmark> boxes,
            List<Integer> classIds,
            List<Float> scores, float scale) {
        List<String> retClasses = new ArrayList<>();
        List<Double> retProbs = new ArrayList<>();
        List<BoundingBox> retBB = new ArrayList<>();

        for (int classId = 0; classId < classes.size(); classId++) {
            List<Rectangle> r = new ArrayList<>();
            List<Double> s = new ArrayList<>();
            List<Integer> map = new ArrayList<>();
            for (int j = 0; j < classIds.size(); ++j) {
                if (classIds.get(j) == classId) {
                    r.add(boxes.get(j));
                    s.add(scores.get(j).doubleValue());
                    map.add(j);
                }
            }
            if (r.isEmpty()) {
                continue;
            }
            List<Integer> nms = Rectangle.nms(r, s, nmsThreshold);
            for (int index : nms) {
                int pos = map.get(index);
                int id = classIds.get(pos);

                int percent = (int) Math.round(scores.get(pos).doubleValue() * 100);
                String className = "face " + percent + "%"; // classes.get(classId)
                retClasses.add(className);
                retProbs.add(scores.get(pos).doubleValue());
//                Rectangle rect = boxes.get(pos);
                Landmark rect = boxes.get(pos);
                List<Point> keypoints = new ArrayList<>();

                //恢复原图坐标（除回比例，减掉 padding）
                rect = LetterBoxUtils.restoreBox(rect, scale, imageWidth, imageHeight, width, height, false);

//                if (removePadding) {
//                    int padW = (width - imageWidth) / 2;
//                    int padH = (height - imageHeight) / 2;
//                    rect.getPath().forEach(point -> {
//                        keypoints.add(new Point(point.getX() - padW, point.getY() - padH));
//                    });
//                    rect =
//                            new Landmark(
//                                    (rect.getX() - padW) / imageWidth,
//                                    (rect.getY() - padH) / imageHeight,
//                                    rect.getWidth() / imageWidth,
//                                    rect.getHeight() / imageHeight,keypoints);
//                } else if (applyRatio) {
//                    rect.getPath().forEach(point -> {
//                        keypoints.add(new Point(point.getX() / width, point.getY() / height));
//                    });
//                    rect =
//                            new Landmark(
//                                    rect.getX() / width,
//                                    rect.getY() / height,
//                                    rect.getWidth() / width,
//                                    rect.getHeight() / height,keypoints);
//                }
                retBB.add(rect);
            }
        }
        return new DetectedObjects(retClasses, retProbs, retBB);
    }

    public static class Builder {

        private int maxBox = 8400;

        YoloOutputType outputType;
        float nmsThreshold;

        protected float threshold = 0.2F;
        protected boolean applyRatio;
        protected boolean removePadding;

        protected int width = 224;
        protected int height = 224;
        protected Image.Flag flag;
        protected Pipeline pipeline;
        protected Batchifier batchifier;

        public Builder() {
            this.outputType = YoloOutputType.AUTO;
            this.nmsThreshold = 0.4F;
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
        public YoloV5FaceTranslator build() {
            if (pipeline == null) {
                addTransform(
                        array -> array.transpose(2, 0, 1).toType(DataType.FLOAT32, false).div(255));
            }
//            validate();
            return new YoloV5FaceTranslator(this);
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

        /** {@inheritDoc} */
        protected void configPostProcess(Map<String, ?> arguments) {
            if (ArgumentsUtil.booleanValue(arguments, "optApplyRatio") || ArgumentsUtil.booleanValue(arguments, "applyRatio")) {
                this.optApplyRatio(true);
            }
            this.threshold = ArgumentsUtil.floatValue(arguments, "threshold", 0.2F);
            String centerFit = ArgumentsUtil.stringValue(arguments, "centerFit", "false");
            this.removePadding = "true".equals(centerFit);
            String type = ArgumentsUtil.stringValue(arguments, "outputType", "AUTO");
            this.outputType = YoloOutputType.valueOf(type.toUpperCase(Locale.ENGLISH));
            this.nmsThreshold = ArgumentsUtil.floatValue(arguments, "nmsThreshold", 0.4F);
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



}
