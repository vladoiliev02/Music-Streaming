package com.vlado.spotify.song;

import com.vlado.spotify.validations.ParameterValidator;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;

public class SongFragment {
    private final byte[] fragment;
    private int read;

    public SongFragment(int size) {
        this.fragment = new byte[size];
    }

    public void read(AudioInputStream audioInputStream) throws IOException {
        ParameterValidator.checkNull(audioInputStream, "audioInputStream");

        read = audioInputStream.read(fragment);
    }

    public byte[] getFragment() {
        return fragment;
    }

    public int getRead() {
        return read;
    }
}
