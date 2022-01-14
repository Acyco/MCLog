package cn.acyco.mclog.mixin;

import cn.acyco.mclog.core.MCLogCore;
import cn.acyco.mclog.ext.AbstractBlockStateExt;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Acyco
 * @create 2022-01-14 18:44
 * @url https://acyco.cn
 */

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockMixin implements AbstractBlockStateExt {

    public BlockState beforeBlockState = null;

    @Inject(method = "onUse", at = @At("RETURN"))
            private void onUse(World world, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {

        if (cir.getReturnValue().isAccepted() && !world.isClient) {
            MCLogCore.onUse(world,player,hand,hit,(AbstractBlock.AbstractBlockState)(Object) this);
        }
    }
    @Inject(method = "onUse", at = @At("HEAD"))
            private void onUseBefore(World world, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {

        if ( !world.isClient) {
            this.setBeforeBlockState(world.getBlockState(hit.getBlockPos()));
        }
    }

    @Override
    public BlockState getBeforeBlockState() {
        return this.beforeBlockState;
    }

    @Override
    public void setBeforeBlockState(BlockState blockState) {
        this.beforeBlockState = blockState;
    }
}
