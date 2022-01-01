package cn.acyco.mclog.mixin;

import cn.acyco.mclog.MCLogCore;
import cn.acyco.mclog.MCLogMod;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	/*@Inject(at = @At("HEAD"), method = "init()V")
	private void init(CallbackInfo info) {
		MCLogMod.LOGGER.info("This line is printed by an example mod mixin!");
	}*/

    @Inject(method = "loadWorld", at = @At("HEAD"))
    private void loadWorld(CallbackInfo ci) {
        MCLogCore.serverLoaded((MinecraftServer) (Object) this);
    }
}
