package Color_yr.AllMusic.player;

import Color_yr.AllMusic.player.decoder.BuffPack;
import Color_yr.AllMusic.player.decoder.IDecoder;
import Color_yr.AllMusic.player.decoder.flac.DataFormatException;
import Color_yr.AllMusic.player.decoder.flac.FlacDecoder;
import Color_yr.AllMusic.player.decoder.mp3.Mp3Decoder;
import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import paulscode.sound.SoundSystem;

import javax.sound.sampled.AudioFormat;
import java.net.URL;
import java.util.Arrays;

public class APlayer {

    private HttpClient client;
    private IDecoder decoder;
    private boolean isClose;
    private SoundSystem sndSystem;
    private String s;

    public APlayer() {
        try {
            SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
            SoundManager soundManager = ObfuscationReflectionHelper.getPrivateValue(SoundHandler.class, handler, "field_147694_f");
            sndSystem = ObfuscationReflectionHelper.getPrivateValue(SoundManager.class, soundManager, "field_148620_e");
            client = HttpClientBuilder.create().useSystemProperties().build();
            isClose = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SetMusic(URL url) throws Exception {
        synchronized (this) {
            try {
                decoder = new FlacDecoder();
                decoder.set(client, url);
            } catch (DataFormatException e) {
                decoder = new Mp3Decoder();
                decoder.set(client, url);
            }
            s = MathHelper.getRandomUUID(ThreadLocalRandom.current()).toString();
            sndSystem.rawDataStream(new AudioFormat(decoder.getOutputFrequency(),
                    16,
                    decoder.getOutputChannels(),
                    true,
                    false), true, s, 0, 0, 0, 0, 16f);
            isClose = false;
        }
    }

    public void play() throws Exception {
        while (true) {
            try {
                if (isClose)
                    break;

                BuffPack output = decoder.decodeFrame();
                if (output == null)
                    break;

                sndSystem.feedRawAudioData(s, Arrays.copyOf(output.buff, output.len));

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        while (true) {
            if (isClose || !sndSystem.playing(s))
                break;
            Thread.sleep(10);
        }
        if (!isClose)
            close();
    }

    public void close() throws Exception {
        isClose = true;
        if(s!=null) {
            sndSystem.stop(s);
            sndSystem.removeSource(s);
        }
        if (decoder != null)
            decoder.close();
    }
}
