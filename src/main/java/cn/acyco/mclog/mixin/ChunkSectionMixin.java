package cn.acyco.mclog.mixin;

import cn.acyco.mclog.ext.TrackServerPlayerExt;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @author Acyco
 * @create 2022-01-20 06:42
 * @url https://acyco.cn
 */
@Mixin(ChunkSection.class)
public abstract class ChunkSectionMixin {
    private static ServerPlayerEntity trackPlayer;

    @Inject(method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;", at = @At("HEAD"))
    private void setBlockStateBefore(int x, int y, int z, BlockState state, boolean lock, CallbackInfoReturnable<BlockState> cir) {
        ServerPlayerEntity player = ((TrackServerPlayerExt) state).getMclogTrackPlayer();
        if (player != null) trackPlayer = player;
    }

    @Inject(method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;", at = @At("RETURN"),locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void setBlockStateBefore(int x, int y, int z, BlockState state, boolean lock, CallbackInfoReturnable<BlockState> cir, BlockState blockState) {
       if(trackPlayer !=null) ((TrackServerPlayerExt)blockState).setMclogTrackPlayer(trackPlayer);
    }
}
