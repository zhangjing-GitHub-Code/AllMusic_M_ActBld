package coloryr.allmusic;

import coloryr.allmusic.hud.HudUtils;
import coloryr.allmusic.player.APlayer;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundSource;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.lwjgl.opengl.GL11;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

@Mod("allmusic")
public class AllMusic {
    private static APlayer nowPlaying;
    private HudUtils HudUtils;
    private String url;

    public AllMusic() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::setup1);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLClientSetupEvent event) {
        SimpleChannel channel = NetworkRegistry.newSimpleChannel(new ResourceLocation("allmusic", "channel"),
                () -> "1.0", s -> true, s -> true);
        channel.registerMessage(666, String.class, this::enc, this::dec, this::proc);
    }

    private void setup1(final FMLLoadCompleteEvent event) {
        nowPlaying = new APlayer();
        HudUtils = new HudUtils();
    }

    private void enc(String str, PacketBuffer buffer) {
        buffer.writeBytes(str.getBytes(StandardCharsets.UTF_8));
    }

    private String dec(PacketBuffer buffer) {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private void proc(String str, Supplier<NetworkEvent.Context> supplier) {
        onClicentPacket(str);
        NetworkEvent.Context context = supplier.get();
        context.setPacketHandled(true);
    }

    @SubscribeEvent
    public void onSound(final SoundEvent.SoundSourceEvent e) {
        if (!nowPlaying.isPlay())
            return;
        SoundCategory data = e.getSound().getCategory();
        switch (data) {
            case MUSIC:
            case RECORDS:
                e.getSource().stop();
        }
    }

    @SubscribeEvent
    public void onServerQuit(final ClientPlayerNetworkEvent.LoggedOutEvent e) {
        try {
            stopPlaying();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        HudUtils.Lyric = HudUtils.Info = HudUtils.List = "";
        HudUtils.haveImg = false;
        HudUtils.save = null;
    }

    private void onClicentPacket(final String message) {
        try {
            if (message.equals("[Stop]")) {
                stopPlaying();
            } else if (message.startsWith("[Play]")) {
                Minecraft.getInstance().getSoundHandler().stop(null, SoundCategory.MUSIC);
                Minecraft.getInstance().getSoundHandler().stop(null, SoundCategory.RECORDS);
                stopPlaying();
                url = message.replace("[Play]", "");
                nowPlaying.setMusic(url);
            } else if (message.startsWith("[Lyric]")) {
                HudUtils.Lyric = message.substring(7);
            } else if (message.startsWith("[Info]")) {
                HudUtils.Info = message.substring(6);
            } else if (message.startsWith("[Img]")) {
                HudUtils.setImg(message.substring(5));
            } else if (message.startsWith("[Pos]")) {
                nowPlaying.set(message.substring(5));
            } else if (message.startsWith("[List]")) {
                HudUtils.List = message.substring(6);
            } else if (message.equalsIgnoreCase("[clear]")) {
                HudUtils.Lyric = HudUtils.Info = HudUtils.List = "";
                HudUtils.haveImg = false;
            } else if (message.startsWith("{")) {
                HudUtils.setPos(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onRed(final RenderGameOverlayEvent.Post e) {
        if (e.getType() == RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            HudUtils.update();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event){
        nowPlaying.tick();
    }

    public static float getVolume() {
        return Minecraft.getInstance().gameSettings.getSoundLevel(SoundCategory.RECORDS);
    }

    public static void drawPic(int textureID, int size, int x, int y) {
        GlStateManager.bindTexture(textureID);
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
        GlStateManager.enableAlphaTest();
    }

    public static void drawText(String item, float x, float y) {
        FontRenderer hud = Minecraft.getInstance().fontRenderer;

        hud.drawStringWithShadow(item, x, y, 0xffffff);
    }

    private void stopPlaying() {
        nowPlaying.closePlayer();
        HudUtils.close();
    }

    public static void runMain(Runnable runnable){
        Minecraft.getInstance().execute(runnable);
    }

    public static void sendMessage(String data) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().player == null)
                return;
            Minecraft.getInstance().player.sendChatMessage(data);
        });
    }
}
