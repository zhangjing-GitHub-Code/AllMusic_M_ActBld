package Color_yr.AllMusic.Hud;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;

public class HudShow {
    public static String Info;
    public static String List;
    public static String Lyric;
    public static SaveOBJ save;

    private static MatrixStack stack = new MatrixStack();

    public static void Set(String data) {
        save = new Gson().fromJson(data, SaveOBJ.class);
    }

    public static void update() {
        InGameHud hud = MinecraftClient.getInstance().inGameHud;
        TextRenderer textRenderer = hud.getFontRenderer();
        if (save == null)
            return;
        if (save.isEnableInfo() && !Info.isEmpty()) {
            int offset = 0;
            String[] temp = Info.split("\n");
            for (String item : temp) {
                textRenderer.drawWithShadow(stack, item,  save.getInfo().getX(),
                         save.getInfo().getY() + offset, 0xffffff);
                offset += 10;
            }
        }
        if (save.isEnableList() && !List.isEmpty()) {
            String[] temp = List.split("\n");
            int offset = 0;
            for (String item : temp) {
                textRenderer.drawWithShadow(stack, item, save.getList().getX(),
                         save.getList().getY() + offset, 0xffffff);
                offset += 10;
            }
        }
        if (save.isEnableLyric() && !Lyric.isEmpty()) {
            String[] temp = Lyric.split("\n");
            int offset = 0;
            for (String item : temp) {
                textRenderer.drawWithShadow(stack, item, save.getLyric().getX(),
                        save.getLyric().getY() + offset, 0xffffff);
                offset += 10;
            }
        }
    }
}
