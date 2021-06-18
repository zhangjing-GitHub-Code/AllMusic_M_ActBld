package Color_yr.AllMusic.player.decoder.flac;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class SeekBufferedInputStream extends BufferedInputStream {
    public SeekBufferedInputStream(@NotNull InputStream in) {
        super(in);
    }

    public void seek(long pos) {
        this.pos = Math.toIntExact(pos);
    }
}
