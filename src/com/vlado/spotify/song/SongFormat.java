package com.vlado.spotify.song;

import com.vlado.spotify.validations.ParameterValidator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.Serializable;
import java.nio.file.Path;

public class SongFormat implements Serializable {
    private final String encoding;
    private final float sampleRate;
    private final int sampleSizeInBits;
    private final int channels;
    private final int frameSize;
    private final float frameRate;
    private final boolean bigEndian;

    public SongFormat(String encoding,
                      float sampleRate,
                      int sampleSizeInBits,
                      int channels,
                      int frameSize,
                      float frameRate,
                      boolean bigEndian) {
        this.encoding = encoding;
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        this.channels = channels;
        this.frameSize = frameSize;
        this.frameRate = frameRate;
        this.bigEndian = bigEndian;
    }

    public static SongFormat of(AudioFormat format) {
        ParameterValidator.checkNull(format, "format");
        return new SongFormat(
                format.getEncoding().toString(),
                format.getSampleRate(),
                format.getSampleSizeInBits(),
                format.getChannels(),
                format.getFrameSize(),
                format.getFrameRate(),
                format.isBigEndian()
        );
    }

    public AudioFormat toAudioFormat() {
        AudioFormat.Encoding encode = switch (this.encoding) {
            case "ALAW" -> AudioFormat.Encoding.ALAW;
            case "PCM_FLOAT" -> AudioFormat.Encoding.PCM_FLOAT;
            case "PCM_SIGNED" -> AudioFormat.Encoding.PCM_SIGNED;
            case "PCM_UNSIGNED" -> AudioFormat.Encoding.PCM_UNSIGNED;
            case "ULAW" -> AudioFormat.Encoding.ULAW;
            default -> throw new IllegalStateException("Unknown audio encoding");
        };

        return new AudioFormat(
                encode,
                this.sampleRate,
                this.sampleSizeInBits,
                this.channels,
                this.frameSize,
                this.frameRate,
                this.bigEndian
        );
    }
}
