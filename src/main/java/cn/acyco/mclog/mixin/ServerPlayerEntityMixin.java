package cn.acyco.mclog.mixin;

import cn.acyco.mclog.core.MCLogCore;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Acyco
 * @create 2022-01-01 13:47
 * @url https://acyco.cn
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends LivingEntity {


    protected ServerPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }


    @Inject(method = "onDeath", at = @At("HEAD"))

    private void onDeath(DamageSource source, CallbackInfo ci) {
        MCLogCore.onPlayerDeath(source, (ServerPlayerEntity)(Object) this);
    }

    @Inject(method = "onDisconnect", at = @At("HEAD"))

    private void onDisconnect(CallbackInfo ci) {
        MCLogCore.onDisconnect((ServerPlayerEntity) (Object) this);
    }
}

