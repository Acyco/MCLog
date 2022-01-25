package cn.acyco.mclog.mixin;

import cn.acyco.mclog.core.MCLogCore;
import cn.acyco.mclog.model.BlockModel;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.LinkedHashSet;

/**
 * @author Acyco
 * @create 2022-01-01 15:51
 * @url https://acyco.cn
 */
@Mixin(value = ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Shadow @Final protected ServerPlayerEntity player;
    @Shadow protected ServerWorld world;
    private LinkedHashSet<BlockModel> trackBlocks = Sets.newLinkedHashSet(); //


    @Inject(method = "tryBreakBlock",
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V",
                    shift = At.Shift.BEFORE
            ))

    private void onBreakBlockBefore(BlockPos pos, CallbackInfoReturnable<Boolean> cir, BlockState blockState, BlockEntity blockEntity, Block block) {
        MCLogCore.onBlockBreakBefore(player, pos,blockState,this.world,trackBlocks);
    }

    @Inject(method = "tryBreakBlock",locals = LocalCapture.CAPTURE_FAILEXCEPTION,at = @At("RETURN"))
    private void onBreakBlockAfter(BlockPos pos, CallbackInfoReturnable<Boolean> cir, BlockState blockState) {
        MCLogCore.onBlockBreakAfter(player,this.world,trackBlocks);
    }

    @Inject(method = "interactBlock",require = 0,cancellable = true, at = @At("RETURN"))
    private void onInteractBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir)
    {
        MCLogCore.onInteractBlock(player, world, stack, hand, hitResult,cir);
    }

}
