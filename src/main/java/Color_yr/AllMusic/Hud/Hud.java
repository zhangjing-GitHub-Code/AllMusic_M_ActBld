package Color_yr.AllMusic.Hud;

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
        if (save == null)
            return;
        if (save.isEnableInfo() && !Info.isEmpty())
            textRenderer.drawWithShadow(Info, save.getInfo().getX(),
                    save.getInfo().getY(), 0xffffff);
        if (save.isEnableList() && !List.isEmpty())
            textRenderer.drawWithShadow(List, save.getList().getX(),
                    save.getList().getY(), 0xffffff);
        if (save.isEnableLyric() && !Lyric.isEmpty())
            textRenderer.drawWithShadow(Lyric, save.getLyric().getX(),
                    save.getLyric().getY(), 0xffffff);
    }
}
