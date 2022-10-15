package coloryr.allmusic;

import coloryr.allmusic.hud.HudUtils;
import coloryr.allmusic.player.APlayer;
import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.nio.charset.StandardCharsets;

public class AllMusic implements ModInitializer {
    public static final Identifier ID = new Identifier("allmusic", "channel");
    public static APlayer nowPlaying;
    public static boolean isPlay = false;
    public static HudUtils hudUtils;

    public static void onServerQuit() {
        try {
            nowPlaying.close();
            hudUtils.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        hudUtils.Lyric = hudUtils.Info = hudUtils.List = "";
        hudUtils.haveImg = false;
        hudUtils.save = null;
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
                    nowPlaying.setMusic(message.replace("[Play]", ""));
                } else if (message.startsWith("[Lyric]")) {
                    hudUtils.Lyric = message.substring(7);
                } else if (message.startsWith("[Info]")) {
                    hudUtils.Info = message.substring(6);
                } else if (message.startsWith("[List]")) {
                    hudUtils.List = message.substring(6);
                } else if (message.startsWith("[Img]")) {
                    hudUtils.setImg(message.substring(5));
                } else if (message.startsWith("[Pos]")) {
                    nowPlaying.set(message.substring(5));
                } else if (message.equalsIgnoreCase("[clear]")) {
                    hudUtils.close();
                } else if (message.startsWith("{")) {
                    hudUtils.setPos(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "allmusic").start();
    }

    private static void stopPlaying() {
        try {
            nowPlaying.close();
            hudUtils.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void drawText(String item, float x, float y){
        InGameHud hud = MinecraftClient.getInstance().inGameHud;
        TextRenderer textRenderer = hud.getFontRenderer();
        textRenderer.drawWithShadow(item, x, y, 0xffffff);
    }

    public static void drawPic(int textureID, int size, int x, int y) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y, 0.0f);
        GL11.glBegin(7);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.0f, (float) size, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f((float) size, (float) size, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f((float) size, 0.0f, 0.0f);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
        GlStateManager.enableAlphaTest();
    }

    public static void sendMessage(String data){
        MinecraftClient.getInstance().execute(() -> {
            if (MinecraftClient.getInstance().player == null)
                return;
            MinecraftClient.getInstance().player.sendChatMessage(data);
        });
    }

    public static void runMain(Runnable runnable){
        MinecraftClient.getInstance().execute(runnable);
    }

    public static float getVolume(){
        return MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.RECORDS);
    }

    public static void reload(){

    }
    @Override
    public void onInitialize() {
        ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buffer, responseSender) -> {
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
        hudUtils = new HudUtils();
    }
}