package Color_yr.ALLMusic_mod.mixin;

import Color_yr.ALLMusic_mod.Hud.Hud;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class GuiShow {
    @Inject(method = "render", at = @At("TAIL"))
    public void Gui(CallbackInfo info) {
        Hud.update();
    }
}
