package cn.smartjavaai.face.utils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * 人脸向量工具类
 * @author dwj
 * @date 2025/5/30
 */
public class VectorUtils {


    /**
     * 将 float 数组转换为 byte 数组
     * @param floats 人脸特征向量
     * @return 转换后的字节数组
     */
    public static byte[] toByteArray(float[] floats) {
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4); // 每个 float 占 4 个字节
        buffer.asFloatBuffer().put(floats);
        return buffer.array();
    }

    /**
     * 将 byte 数组转换回 float 数组
     * @param bytes 从数据库读取的字节数组
     * @return 原始的人脸特征向量
     */
    public static float[] toFloatArray(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return new float[0];
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        FloatBuffer floatBuffer = buffer.asFloatBuffer();
        float[] floats = new float[floatBuffer.remaining()];
        floatBuffer.get(floats);
        return floats;
    }

}
