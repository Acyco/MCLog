package cn.acyco.mclog.mixin;

import cn.acyco.mclog.core.MCLogCore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

/**
 * @author Acyco
 * @create 2022-01-19 23:26
 * @url https://acyco.cn
 */
@Mixin(Explosion.class)
public abstract class ExplosionMixin {


    @Shadow @Final private World world;

    @Shadow @Final private @Nullable Entity entity;

    @Inject(method = "affectWorld", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"
    ))
    private void onAffectWorld(boolean particles, CallbackInfo ci, boolean bl, ObjectArrayList objectArrayList, Iterator var4, BlockPos blockPos, BlockState blockState, Block block) {
        MCLogCore.onAffectWorld(this.entity,this.world,blockPos,blockState);
    }
}
