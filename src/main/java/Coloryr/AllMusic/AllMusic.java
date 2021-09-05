package Coloryr.AllMusic;

import Coloryr.AllMusic.Hud.HudUtils;
import Coloryr.AllMusic.player.APlayer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AllMusic implements ModInitializer {
    public static final Identifier ID = new Identifier("allmusic", "channel");
    private static APlayer nowPlaying;
    public static boolean isPlay = false;
    private static URL nowURL;
    public static HudUtils HudUtils;

    public static void onServerQuit() {
        try {
            stopPlaying();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        HudUtils.Lyric = HudUtils.Info = HudUtils.List = "";
        HudUtils.haveImg = false;
        HudUtils.save = null;
    }

    public static URL Get(URL url) {
        if (url.toString().contains("https://music.163.com/song/media/outer/url?id=")
                || url.toString().contains("http://music.163.com/song/media/outer/url?id=")) {
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(4 * 1000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36 Edg/84.0.522.52");
                connection.setRequestProperty("Host", "music.163.com");
                connection.connect();
                if (connection.getResponseCode() == 302) {
                    return new URL(connection.getHeaderField("Location"));
                }
                return connection.getURL();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    public static void onClicentPacket(final String message) {
        new Thread(() -> {
            try {
                if (message.equals("[Stop]")) {
                    stopPlaying();
                } else if (message.startsWith("[Play]")) {
                    MinecraftClient.getInstance().getSoundManager().stopSounds(null, SoundCategory.MUSIC);
                    MinecraftClient.getInstance().getSoundManager().stopSounds(null, SoundCategory.RECORDS);
                    stopPlaying();
                    nowURL = new URL(message.replace("[Play]", ""));
                    nowURL = Get(nowURL);
                    if (nowURL == null)
                        return;
                    stopPlaying();
                    nowPlaying.SetMusic(nowURL);
                } else if (message.startsWith("[Lyric]")) {
                    HudUtils.Lyric = message.substring(7);
                } else if (message.startsWith("[Info]")) {
                    HudUtils.Info = message.substring(6);
                } else if (message.startsWith("[List]")) {
                    HudUtils.List = message.substring(6);
                } else if (message.startsWith("[Img]")) {
                    HudUtils.SetImg(message.substring(5));
                } else if (message.startsWith("[Pos]")) {
                    nowPlaying.set(message.substring(5));
                } else if (message.equalsIgnoreCase("[clear]")) {
                    HudUtils.Lyric = HudUtils.Info = HudUtils.List = "";
                    HudUtils.haveImg = false;
                } else if (message.startsWith("{")) {
                    HudUtils.Set(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "allmusic").start();
    }

    private static void stopPlaying() {
        nowPlaying.close();
        HudUtils.stop();
    }

    @Override
    public void onInitialize() {
        ClientSidePacketRegistry.INSTANCE.register(ID, (context, buffer) -> {
            try {
                byte[] buff = new byte[buffer.readableBytes()];
                buffer.readBytes(buff);
                buff[0] = 0;
                String data = new String(buff, StandardCharsets.UTF_8).substring(1);
                onClicentPacket(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        nowPlaying = new APlayer();
        HudUtils = new HudUtils();
    }
}
