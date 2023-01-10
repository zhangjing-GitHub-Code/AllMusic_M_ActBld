package coloryr.allmusic;

import coloryr.allmusic.hud.HudUtils;
import coloryr.allmusic.player.APlayer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledExecutorService;

public class AllMusic implements ModInitializer {
    public static final Identifier ID = new Identifier("allmusic", "channel");
    public static APlayer nowPlaying;
    public static String url;
    public static HudUtils hudUtils;
    private static int ang = 0;
    private static int count = 0;

    private static ScheduledExecutorService service;

    public static void onServerQuit() {
        try {
            stopPlaying();
        } catch (Exception e) {
            e.printStackTrace();
        }
        hudUtils.Lyric = hudUtils.Info = hudUtils.List = "";
        hudUtils.haveImg = false;
        hudUtils.save = null;
    }

    public static void onClicentPacket(final String message) {
        try {
            if (message.equals("[Stop]")) {
                stopPlaying();
            } else if (message.startsWith("[Play]")) {
                MinecraftClient.getInstance().getSoundManager().stopSounds(null, SoundCategory.MUSIC);
                MinecraftClient.getInstance().getSoundManager().stopSounds(null, SoundCategory.RECORDS);
                stopPlaying();
                url = message.replace("[Play]", "");
                nowPlaying.setMusic(url);
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
    }

    private static void stopPlaying() {
        nowPlaying.closePlayer();
        hudUtils.close();
    }

    private static MatrixStack stack = new MatrixStack();

    public static void drawText(String item, float x, float y){
        var hud = MinecraftClient.getInstance().inGameHud;
        var textRenderer = hud.getTextRenderer();
        textRenderer.drawWithShadow(stack, item, x, y, 0xffffff);
    }

    public static void drawPic(int textureID, int size, int x, int y) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, textureID);
        DrawableHelper.drawTexture(stack, x, y,
                0, 0, 0, size, size, size, size);
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

    private static void time1() {
        if (hudUtils.save == null)
            return;
        if (count < hudUtils.save.PicRotateSpeed) {
            count++;
            return;
        }
        count = 0;
        ang++;
        ang = ang % 360;
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

        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(AllMusic::time1, 0, 1, TimeUnit.MILLISECONDS);
    }
}