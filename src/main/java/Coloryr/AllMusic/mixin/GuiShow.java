package Coloryr.AllMusic.mixin;

import Coloryr.AllMusic.AllMusic;
import Coloryr.AllMusic.Hud.HudUtils;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class GuiShow {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getLastFrameDuration()F"))
    public void Gui(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        AllMusic.HudUtils.update(matrices);
    }
}
