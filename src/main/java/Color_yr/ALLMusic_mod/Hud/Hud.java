package Color_yr.ALLMusic_mod.Hud;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;

public class Hud {
    public static String Info;
    public static String List;
    public static String Lyric;
    public static SaveOBJ save;

    public static void Set(String data) {
        save = new Gson().fromJson(data, SaveOBJ.class);
    }

    public static void update() {
        InGameHud hud = MinecraftClient.getInstance().inGameHud;
        TextRenderer textRenderer = hud.getFontRenderer();
        if (save.isEnableInfo() && !Info.isEmpty())
            textRenderer.drawWithShadow(Info, save.getInfo().getX(),
                    save.getInfo().getY(), 0xffffff);
        if (save.isEnableList() && !List.isEmpty())
            textRenderer.drawWithShadow(Info, save.getList().getX(),
                    save.getInfo().getY(), 0xffffff);
        if (save.isEnableLyric() && !Lyric.isEmpty())
            textRenderer.drawWithShadow(Info, save.getLyric().getX(),
                    save.getInfo().getY(), 0xffffff);
    }
}
