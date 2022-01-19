package cn.acyco.mclog.mixin;

import net.minecraft.entity.mob.CreeperEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Acyco
 * @create 2022-01-19 22:41
 * @url https://acyco.cn
 */
@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin {
    @Inject(method= "tick" ,at = @At(
            value = "INVOKE",
            target= "Lnet/minecraft/entity/mob/CreeperEntity;explode()V"
    ))

    private void onExplode(CallbackInfo ci) {
       // MCLogCore.onCreeperExplode((CreeperEntity) (Object)this);

    }
}
