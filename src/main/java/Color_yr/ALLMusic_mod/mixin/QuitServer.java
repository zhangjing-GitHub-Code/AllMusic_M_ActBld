package Color_yr.ALLMusic_mod.mixin;

import Color_yr.ALLMusic_mod.ALLMusic_mod;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class QuitServer {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("TAIL"))
    public void Quit(CallbackInfo info) {
        ALLMusic_mod.onServerQuit();
    }
}
