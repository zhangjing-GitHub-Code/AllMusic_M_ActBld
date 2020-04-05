package javazoom.jl.player;

import javax.sound.sampled.AudioFormat;

public class AudioFormatSelf extends AudioFormat {
    public AudioFormatSelf(int outputFrequency, int i, int outputChannels, boolean b, boolean b1) {
        super(outputFrequency, i, outputChannels, b, b1);
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }
}
