package cn.acyco.mclog.mixin;

import cn.acyco.mclog.core.MCLogCore;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	/*@Inject(at = @At("HEAD"), method = "init()V")
	private void init(CallbackInfo info) {
		MCLogMod.LOGGER.info("This line is printed by an example mod mixin!");
	}*/

    @Inject(method = "loadWorld", at = @At("HEAD"))
    private void loadWorld(CallbackInfo ci) {
        MCLogCore.serverLoaded((MinecraftServer) (Object) this);
    }

    @Inject(method = "loadWorld", at = @At("RETURN"))
    private void serverLoadedWorlds(CallbackInfo ci) {
        MCLogCore.serverLoadedWorlds((MinecraftServer) (Object) this);
    }
 /*   @Redirect(method = "createWorlds", at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private <K, V> V onLoadWorld(Map<K,V> worlds, K registryKey, V serverWorld) {
        final V result = worlds.put(registryKey, serverWorld);
        MCLogCore.onLoadWorld(registryKey, serverWorld);

        return result;
    }*/


    @Inject(method = "shutdown", at = @At("HEAD"))
    private void shutdown(CallbackInfo ci) {
        MCLogCore.serverShutdown();
    }
}
