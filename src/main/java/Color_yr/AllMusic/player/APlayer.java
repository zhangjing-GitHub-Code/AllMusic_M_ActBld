package Color_yr.AllMusic.player;

import Color_yr.AllMusic.player.decoder.BuffPack;
import Color_yr.AllMusic.player.decoder.IDecoder;
import Color_yr.AllMusic.player.decoder.flac.DataFormatException;
import Color_yr.AllMusic.player.decoder.flac.FlacDecoder;
import Color_yr.AllMusic.player.decoder.mp3.Mp3Decoder;
import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.system.MemoryUtil;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class APlayer {

    private HttpClient client;
    private IDecoder decoder;
    private boolean isClose;
    private ChannelManager sndSystem;
    private ChannelManager.Entry channelmanager;
    private AudioFormat audioformat;
    private int index;
    private boolean init;

    public APlayer() {
        try {
            SoundHandler handler = Minecraft.getInstance().getSoundHandler();
            SoundEngine soundManager = ObfuscationReflectionHelper.getPrivateValue(SoundHandler.class, handler, "field_147694_f");
            sndSystem = ObfuscationReflectionHelper.getPrivateValue(SoundEngine.class, soundManager, "field_217941_k");
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
            audioformat = new AudioFormat(decoder.getOutputFrequency(),
                    16,
                    decoder.getOutputChannels(),
                    true,
                    false);
            if (channelmanager == null) {
                channelmanager = sndSystem.createChannel(SoundSystem.Mode.STREAMING);
                channelmanager.runOnSoundExecutor((ex) -> {
                    index = ObfuscationReflectionHelper.getPrivateValue(SoundSource.class, ex, "field_216441_b");
                    init = true;
                });
            }
            while (!init) ;
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

                int[] aint = new int[1];
                AL10.alGenBuffers(aint);
                int buff = aint[0];
                ByteBuffer buffer = ByteBuffer.wrap(output.buff, 0, output.len);
                AL10.alBufferData(buff, AL10.AL_FORMAT_STEREO16,buffer , (int) audioformat.getSampleRate());
                AL10.alSourceQueueBuffers(index, buff);
                int m_numprocessed = AL10.alGetSourcei(index, AL10.AL_BUFFERS_PROCESSED);
                int m_numqueued = AL10.alGetSourcei(index, AL10.AL_BUFFERS_QUEUED);
                int stateVaue = AL10.alGetSourcei(index, AL10.AL_SOURCE_STATE);

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        AL10.alSourcePlay(index);
        while (true) {
            if (isClose)
                break;
            Thread.sleep(10);
        }
        if (!isClose)
            close();
    }

    public void close() throws Exception {
        isClose = true;
        if (channelmanager != null) {
            channelmanager.runOnSoundExecutor(SoundSource::func_216418_f);
            channelmanager.release();
        }
        if (decoder != null)
            decoder.close();
    }
}
