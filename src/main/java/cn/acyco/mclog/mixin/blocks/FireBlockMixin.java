package cn.acyco.mclog.mixin.blocks;

import cn.acyco.mclog.core.MCLogCore;
import cn.acyco.mclog.ext.TrackServerPlayerExt;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

/**
 * @author Acyco
 * @create 2022-01-20 00:56
 * @url https://acyco.cn
 */

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {
    private ServerPlayerEntity player;

    @Inject(method = "scheduledTick", at = @At("HEAD"))
    private void onScheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (world.isClient) {
            return;
        }
        BlockState blockState = world.getBlockState(pos);
        ServerPlayerEntity playerEntity = ((TrackServerPlayerExt) blockState).getMclogTrackPlayer();
        if (playerEntity != null) {
            this.player = playerEntity;
        }

    }

    @Inject(method = "scheduledTick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z",
            shift = At.Shift.BEFORE
    ))
    private void setServerWorld(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (world.isClient) {
            return;
        }
        ((TrackServerPlayerExt) state).setMclogTrackPlayer(player);
    }


    @Inject(method = "trySpreadingFire", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z",
            shift = At.Shift.BEFORE
    ))
    private void tryRemoveBlock(World world, BlockPos pos, int spreadFactor, Random rand, int currentAge, CallbackInfo ci) {
        MCLogCore.fireBlockTryRemoveBlockOrTrySetBlockState( world, pos,player);
    }
    @Inject(method = "trySpreadingFire", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z",
            shift = At.Shift.BEFORE
    ))
    private void trySetBlockState(World world, BlockPos pos, int spreadFactor, Random rand, int currentAge, CallbackInfo ci) {
        MCLogCore.fireBlockTryRemoveBlockOrTrySetBlockState( world, pos,player);
    }
}
