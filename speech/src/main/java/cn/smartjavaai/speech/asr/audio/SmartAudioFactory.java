package cn.smartjavaai.speech.asr.audio;

import ai.djl.modality.audio.Audio;
import ai.djl.modality.audio.AudioFactory;
import cn.smartjavaai.speech.asr.exception.AsrException;
import cn.smartjavaai.speech.utils.AudioUtils;
import lombok.extern.slf4j.Slf4j;
import ws.schild.jave.EncoderException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author dwj
 * @date 2025/8/1
 */
@Slf4j
public class SmartAudioFactory {


    private static final SmartAudioFactory INSTANCE = new SmartAudioFactory();

    private SmartAudioFactory() {
    }

    public static SmartAudioFactory getInstance() {
        return INSTANCE;
    }



    public Audio fromFile(Path path, AudioFormat targetFormat) throws IOException {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(path.toFile())) {
            if(targetFormat != null){
                try (AudioInputStream convertedAis = AudioSystem.getAudioInputStream(targetFormat, ais);) {
                    byte[] bytes = read(convertedAis);
                    float[] floats = bytesToFloats(bytes, targetFormat.isBigEndian());
                    return new Audio(floats, targetFormat.getSampleRate(), targetFormat.getChannels());
                }
            }else{
                AudioFormat format = ais.getFormat();
                byte[] bytes = read(ais);
                float[] floats = bytesToFloats(bytes, format.isBigEndian());
                return new Audio(floats, format.getSampleRate(), format.getChannels());
            }
        } catch (UnsupportedAudioFileException e) {
            log.debug("Unsupported Audio file, Conversion to WAV is required");
            byte[] allBytes = Files.readAllBytes(path);
            try(AudioInputStream audioInputStream = convertWav(allBytes)){
                return fromAudioInputStream(audioInputStream, targetFormat);
            }
        }
    }

    public Audio fromAudioInputStream(AudioInputStream ais, AudioFormat targetFormat) throws IOException {
        try (AudioInputStream audioInputStream = ais) {
            if(targetFormat != null){
                try (AudioInputStream convertedAis = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);) {
                    byte[] bytes = read(convertedAis);
                    float[] floats = bytesToFloats(bytes, targetFormat.isBigEndian());
                    return new Audio(floats, targetFormat.getSampleRate(), targetFormat.getChannels());
                }
            }else{
                AudioFormat format = audioInputStream.getFormat();
                byte[] bytes = read(audioInputStream);
                float[] floats = bytesToFloats(bytes, format.isBigEndian());
                return new Audio(floats, format.getSampleRate(), format.getChannels());
            }
        }
    }

    public AudioInputStream convertWav(byte[] allBytes) {
        InputStream conversionStream = new BufferedInputStream(new ByteArrayInputStream(allBytes));
        File tempFile = null;
        try {
            tempFile = AudioUtils.audioFormatConversion(conversionStream, "wav");
            InputStream fis = new BufferedInputStream(new FileInputStream(tempFile));
            AudioInputStream ais = AudioSystem.getAudioInputStream(fis);
            return ais;
        } catch (EncoderException | IOException | UnsupportedAudioFileException e) {
            throw new AsrException(e);
        } finally {
            if(tempFile != null && tempFile.exists()){
                tempFile.delete();
            }
            if(conversionStream != null){
                try {
                    conversionStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public Audio fromInputStream(InputStream is, AudioFormat targetFormat) throws IOException {
        byte[] allBytes = is.readAllBytes();
        try (BufferedInputStream tryStream = new BufferedInputStream(new ByteArrayInputStream(allBytes));
                AudioInputStream ais = AudioSystem.getAudioInputStream(tryStream)) {
            if(targetFormat != null){
                try (AudioInputStream convertedAis = AudioSystem.getAudioInputStream(targetFormat, ais);) {
                    byte[] bytes = read(convertedAis);
                    float[] floats = bytesToFloats(bytes, targetFormat.isBigEndian());
                    return new Audio(floats, targetFormat.getSampleRate(), targetFormat.getChannels());
                }
            }else{
                AudioFormat format = ais.getFormat();
                byte[] bytes = read(ais);
                float[] floats = bytesToFloats(bytes, format.isBigEndian());
                return new Audio(floats, format.getSampleRate(), format.getChannels());
            }
        } catch (UnsupportedAudioFileException e) {
            log.debug("Unsupported Audio file, Conversion to WAV is required");
            try(AudioInputStream audioInputStream = convertWav(allBytes)){
                return fromAudioInputStream(audioInputStream, targetFormat);
            }
        }
    }

    private byte[] read(AudioInputStream ais) throws IOException {
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

    private float[] bytesToFloats(byte[] bytes, boolean isBigEndian) {
        ByteOrder order = isBigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        ShortBuffer buffer = ByteBuffer.wrap(bytes).order(order).asShortBuffer();
        short[] shorts = new short[buffer.capacity()];
        buffer.get(shorts);

        // Feed in float values between -1.0f and 1.0f.
        float[] floats = new float[shorts.length];
        for (int i = 0; i < shorts.length; i++) {
            floats[i] = ((float) shorts[i]) / (float) Short.MAX_VALUE;
        }
        return floats;
    }

}
