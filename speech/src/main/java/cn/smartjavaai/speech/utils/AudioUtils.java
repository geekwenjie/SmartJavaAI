package cn.smartjavaai.speech.utils;

import ai.djl.modality.audio.Audio;
import cn.hutool.core.lang.UUID;
import cn.smartjavaai.speech.asr.exception.AsrException;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.MultimediaInfo;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.file.Files;

/**
 * 音频工具类
 * @author dwj
 */
public class AudioUtils {


    public static byte[] read(AudioInputStream ais) throws IOException {
        AudioFormat format = ais.getFormat();
        int frameSize = format.getFrameSize();

        // Some audio formats may have unspecified frame size
        if (frameSize == AudioSystem.NOT_SPECIFIED) {
            frameSize = 1;
        }
        int size = (int) ais.getFrameLength() * frameSize;
        if (ais.getFrameLength() == AudioSystem.NOT_SPECIFIED || size <= 0){
            // unknown length, use ByteArrayOutputStream to read all data
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int read;
            while ((read = ais.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            return baos.toByteArray();
        }else{
            byte[] ret = new byte[size];
            byte[] buf = new byte[1024];
            int offset = 0;
            int read;
            while ((read = ais.read(buf)) != -1) {
                System.arraycopy(buf, 0, ret, offset, read);
                offset += read;
            }
            return ret;
        }
    }

    /**
     * 音频格式转换
     *
     * @param sourceFilePath
     * @param targetFilePath
     * @param format            wav/mp3/amr
     * @return
     */
    public static byte[] getAudioFormatConversionBytes(String sourceFilePath,String targetFilePath,String format) {
        InputStream fis = null;
        ByteArrayOutputStream bos = null;
        byte[] bytes = null;
        try {
            File sourceFile = new File(sourceFilePath);
            if (sourceFile.isFile()) {
                File targetFile = new File(targetFilePath);

                // 音频格式转换
                audioFormatConversion(sourceFile, targetFile, format);

                fis = new FileInputStream(targetFile);
                bos = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                bytes = bos.toByteArray();
            }
        } catch (Exception e) {
            throw new AsrException("音频格式转换异常：" + e.getMessage(), e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                throw new AsrException("音频格式转换资源关闭异常：" + e.getMessage(), e);
            }
        }
        return bytes;
    }

    /**
     * 音频格式转换
     *
     * @param sourceFilePath
     * @param targetFilePath
     * @param format            wav/mp3/amr
     * @return
     */
    public static InputStream getAudioFormatConversionIns(String sourceFilePath, String targetFilePath, String format) throws EncoderException, FileNotFoundException {
        File sourceFile = new File(sourceFilePath);
        if (sourceFile.isFile()) {
            File targetFile = new File(targetFilePath);
            // 音频格式转换
            audioFormatConversion(sourceFile, targetFile, format);
            return new FileInputStream(targetFile);
        }
        return null;
    }

    /**
     * 音频格式转换
     * @param source 源音频文件
     * @param target 输出的音频文件
     * @param format wav/mp3/amr
     */
    public static void audioFormatConversion(File source,File target,String format) throws EncoderException {
        //Audio Attributes
        AudioAttributes audio = new AudioAttributes();
        switch (format) {
            case "wav":
                audio.setCodec("pcm_s16le");
                break;
            case "mp3":
                audio.setCodec("libmp3lame");
                break;
            case "amr":
                audio.setCodec("libvo_amrwbenc");
                break;
            default:
                throw new IllegalArgumentException("不支持的音频格式：" + format);
        }
        audio.setBitRate(16000);
        audio.setChannels(1);
        audio.setSamplingRate(16000);
        //Encoding attributes
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setOutputFormat(format);
        attrs.setAudioAttributes(audio);
        //Encode
        Encoder encoder = new Encoder();
        encoder.encode(new MultimediaObject(source), target, attrs);
    }


    /**
     * 音频格式转换
     * @param sourceStream 源音频流
     * @param format wav/mp3/amr
     * @return
     */
    public static File audioFormatConversion(InputStream sourceStream, String format) throws EncoderException, IOException {
        // 1. 写入临时源文件
        File sourceFile = Files.createTempFile("source-", ".tmp").toFile();
        try (OutputStream os = new FileOutputStream(sourceFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = sourceStream.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        }
        // 2. 创建临时目标文件
        File targetFile = Files.createTempFile("target-", "." + format).toFile();
        // 3. 调用已有方法进行格式转换
        audioFormatConversion(sourceFile, targetFile, format);
        // 4. 删除源文件（可选）
        sourceFile.delete();
        return targetFile;
    }

    /**
     * 获取音频信息
     * @param source
     * @return
     */
    public static MultimediaInfo getAudioInfo(File source) throws EncoderException {
        MultimediaObject mo = new MultimediaObject(source);
        return mo.getInfo();
    }

    /**
     * 获取音频信息
     * @param ais
     * @return
     */
    public static MultimediaInfo getAudioInfo(AudioInputStream ais) throws EncoderException, IOException {
        File tempFile = Files.createTempFile("audio_" + UUID.fastUUID().toString(), null).toFile();
        tempFile.deleteOnExit(); // JVM退出时自动删除
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = ais.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return getAudioInfo(tempFile);
    }


    /**
     * 将 float[] 音频数据保存为 WAV 文件
     */
    public static void saveToWav(float[] floats, AudioFormat format, String savePath) throws IOException {
        // 1. 转换为 16-bit PCM
        byte[] bytes = floatsToPCM16(floats);
        // 2. 使用 ByteArrayInputStream 封装为音频流
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             AudioInputStream ais = new AudioInputStream(bais, format, floats.length)) {
            // 3. 保存到本地文件
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(savePath));
        }
    }

    /**
     * 将 float[] 音频数据保存为 WAV 文件
     */
    public static void saveToWav(float[] floats, String savePath) throws IOException {
        // 1. 转换为 16-bit PCM
        byte[] bytes = floatsToPCM16(floats);
        // 2. 使用 ByteArrayInputStream 封装为音频流
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             AudioInputStream ais = new AudioInputStream(bais, getDefaultAudioFormat(), floats.length)) {
            // 3. 保存到本地文件
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(savePath));
        }
    }

    /**
     * 将 float[] 音频数据保存为 WAV 文件
     */
    public static void saveToWav(Audio audio, String savePath) throws IOException {
        AudioFormat audioFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                audio.getSampleRate(),
                16,
                1,
                2,
                audio.getSampleRate(),
                false
        );
        // 1. 转换为 16-bit PCM
        byte[] bytes = floatsToPCM16(audio.getData());
        // 2. 使用 ByteArrayInputStream 封装为音频流
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             AudioInputStream ais = new AudioInputStream(bais, audioFormat, audio.getData().length)) {
            // 3. 保存到本地文件
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(savePath));
        }
    }


    public static AudioFormat getDefaultAudioFormat(){
        return new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                16000,
                16,
                1,
                2,
                16000,
                false
        );
    }

    /**
     * 将 float[] 转为 16bit PCM (little endian)
     */
    private static byte[] floatsToPCM16(float[] floats) {
        byte[] bytes = new byte[floats.length * 2];
        int i = 0;
        for (float sample : floats) {
            // 裁剪范围 [-1, 1]
            sample = Math.max(-1.0f, Math.min(1.0f, sample));
            short s = (short) (sample * 32767);
            bytes[i++] = (byte) (s & 0xFF);
            bytes[i++] = (byte) ((s >> 8) & 0xFF);
        }
        return bytes;
    }



}
