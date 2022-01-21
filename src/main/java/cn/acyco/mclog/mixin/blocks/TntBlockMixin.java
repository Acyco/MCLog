package cn.acyco.mclog.mixin.blocks;

import cn.acyco.mclog.core.MCLogCore;
import cn.acyco.mclog.ext.TrackServerPlayerExt;
import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @author Acyco
 * @create 2022-01-20 01:27
 * @url https://acyco.cn
 */
@Mixin(TntBlock.class)
public class TntBlockMixin {
    private static ServerPlayerEntity tempPlayer; //出tnt方块实体前他会清方块状态，先保存

    @Inject(method = "primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"))
    private static void primeTnt(World world, BlockPos pos, LivingEntity igniter, CallbackInfo ci) {
        if (world.isClient()) {
            return;
        }
        TrackServerPlayerExt trackServerPlayer = (TrackServerPlayerExt) world.getBlockState(pos);
        tempPlayer = trackServerPlayer.getMclogTrackPlayer();
    }

    @Inject(method = "primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/LivingEntity;)V", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z",
            shift = At.Shift.BEFORE
    ))
    private static void spawnEntity(World world, BlockPos pos, LivingEntity igniter, CallbackInfo ci, TntEntity tntEntity) {
        if (world.isClient()) {
            return;
        }
        if (igniter != null) {
            //直接点tnt
            ((TrackServerPlayerExt) tntEntity).setMclogTrackPlayer((ServerPlayerEntity) igniter);
            return;
        }
        ((TrackServerPlayerExt) tntEntity).setMclogTrackPlayer(tempPlayer); //火焰蔓延引起的tnt爆炸


    }

    @Inject(method = "onUse", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z",
            shift = At.Shift.BEFORE
    )
    )
    private void igniteTnt(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {

        MCLogCore.igniteTnt(state, world, pos, player);
    }
}
