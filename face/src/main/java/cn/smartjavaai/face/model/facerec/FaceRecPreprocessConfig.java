package cn.smartjavaai.face.model.facerec;

import ai.djl.modality.cv.Image;
import lombok.Data;

/**
 * 人脸识别参数
 * @author dwj
 * @date 2025/9/18
 */
@Data
public class FaceRecPreprocessConfig {

    private int inputWidth;
    private int inputHeight;

    /**
     * 像素类型
     */
    private Image.Flag imageFlag;

    /**
     * 是否使用管道
     */
    private boolean usePipeline;

    /**
     * 是否归一化
     */
    private boolean normalize;

    /**
     * 归一化 mean
     */
    private float[] mean;

    /**
     * 归一化 std
     */
    private float[] std;

    /**
     * 输出索引（默认取第 0 个）
     */
    private int outputIndex;

    private FaceRecPreprocessConfig(Builder builder) {
        this.inputWidth = builder.inputWidth;
        this.inputHeight = builder.inputHeight;
        this.imageFlag = builder.imageFlag;
        this.usePipeline = builder.usePipeline;
        this.normalize = builder.normalize;
        this.mean = builder.mean;
        this.std = builder.std;
        this.outputIndex = builder.outputIndex;
    }

    // ========= Builder =========
    public static class Builder {
        private int inputWidth = 112;   // 默认值
        private int inputHeight = 112;  // 默认值
        private Image.Flag imageFlag = Image.Flag.COLOR; // 默认彩色
        private boolean usePipeline = true;
        private boolean normalize = true;

        private float[] mean = new float[]{0.5F, 0.5F, 0.5F};
        private float[] std = new float[]{0.5F, 0.5F, 0.5F};
        private int outputIndex;


        public Builder inputSize(int width, int height) {
            this.inputWidth = width;
            this.inputHeight = height;
            return this;
        }

        public Builder imageFlag(Image.Flag flag) {
            this.imageFlag = flag;
            return this;
        }

        public Builder usePipeline(boolean usePipeline) {
            this.usePipeline = usePipeline;
            return this;
        }

        public Builder normalize(boolean normalize) {
            this.normalize = normalize;
            return this;
        }

        public Builder mean(float... mean) {
            this.mean = mean;
            return this;
        }

        public Builder std(float... std) {
            this.std = std;
            return this;
        }

        public Builder outputIndex(int outputIndex) {
            this.outputIndex = outputIndex;
            return this;
        }

        public FaceRecPreprocessConfig build() {
            return new FaceRecPreprocessConfig(this);
        }
    }



}
