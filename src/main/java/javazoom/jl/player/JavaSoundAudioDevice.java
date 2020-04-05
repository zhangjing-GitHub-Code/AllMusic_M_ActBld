/*
 * 11/26/04		Buffer size modified to support JRE 1.5 optimizations.
 *              (CPU usage < 1% under P4/2Ghz, RAM < 12MB).
 *              jlayer@javazoom.net
 * 11/19/04		1.0 moved to LGPL.
 * 06/04/01		Too fast playback fixed. mdm@techie.com
 * 29/01/00		Initial version. mdm@techie.com
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package javazoom.jl.player;

import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.JavaLayerException;

import javax.sound.sampled.*;

/**
 * The <code>JavaSoundAudioDevice</code> implements an audio
 * device by using the JavaSound API.
 *
 * @author Mat McGowan
 * @since 0.0.8
 */
public class JavaSoundAudioDevice {
    private SourceDataLine source = null;
    private boolean open = false;
    private Decoder decoder = null;
    private AudioFormatSelf fmt = null;

    private byte[] byteBuf = new byte[4096];

    private FloatControl volctrl;

    public FloatControl getVolctrl() {
        return volctrl;
    }

    public synchronized void open(Decoder decoder) {
        while (open) {
           close();
        }
        this.decoder = decoder;
        open = true;
    }

    public synchronized boolean isOpen() {
        return open;
    }

    protected AudioFormatSelf getAudioFormat() {
        if (fmt == null) {
            fmt = new AudioFormatSelf(decoder.getOutputFrequency(),
                    16,
                    decoder.getOutputChannels(),
                    true,
                    false);
        } else {
            fmt.setSampleRate(decoder.getOutputFrequency());
            fmt.setChannels(decoder.getOutputChannels());
        }
        return fmt;
    }

    public synchronized void close() {
        if (open) {
            open = false;
        }
        if (source != null)
            source.close();
        decoder = null;
    }

    public void write(short[] samples, int offs, int len)
            throws JavaLayerException {
        if (isOpen()) {
            writeImpl(samples, offs, len);
        }
    }

    public void flush() {
        flushImpl();
    }

    protected void flushImpl() {
        if (source != null) {
            source.drain();
        }
    }

    protected DataLine.Info getSourceLineInfo() {
        AudioFormat fmt = getAudioFormat();
        return new DataLine.Info(SourceDataLine.class, fmt);
    }

    // createSource fix.
    protected void createSource() throws JavaLayerException {
        Throwable t = null;
        try {
            Line line = AudioSystem.getLine(getSourceLineInfo());
            if (line instanceof SourceDataLine) {
                source = (SourceDataLine) line;
                source.open(fmt);
                source.start();
                volctrl = (FloatControl) source.getControl(FloatControl.Type.MASTER_GAIN);
            }
        } catch (RuntimeException | LinkageError | LineUnavailableException ex) {
            t = ex;
        }
        if (source == null) throw new JavaLayerException("cannot obtain source audio line", t);
    }

    protected void writeImpl(short[] samples, int offs, int len)
            throws JavaLayerException {
        if (source == null)
            createSource();

        byte[] b = toByteArray(samples, offs, len);
        source.write(b, 0, len * 2);
    }

    protected byte[] getByteArray(int length) {
        if (byteBuf.length < length) {
            byteBuf = new byte[length + 1024];
        }
        return byteBuf;
    }

    protected byte[] toByteArray(short[] samples, int offs, int len) {
        byte[] b = getByteArray(len * 2);
        int idx = 0;
        short s;
        while (len-- > 0) {
            s = samples[offs++];
            b[idx++] = (byte) s;
            b[idx++] = (byte) (s >>> 8);
        }
        return b;
    }

    public int getPosition() {
        int pos = 0;
        if (source != null) {
            pos = (int) (source.getMicrosecondPosition() / 1000);
        }
        return pos;
    }

}
