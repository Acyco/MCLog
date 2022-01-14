package cn.acyco.mclog.mixin;

import cn.acyco.mclog.core.MCLogCore;
import cn.acyco.mclog.ext.BlockItemExt;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Acyco
 * @create 2022-01-01 16:48
 * @url https://acyco.cn
 */
@Mixin(BlockItem.class)
public abstract class BlockItemMixin implements BlockItemExt {
    private BlockState beforeState;
    private FluidState fluidState;

    @Inject(
            method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/ActionResult;success(Z)Lnet/minecraft/util/ActionResult;"
            ),
            cancellable = true
    )
    private void onBlockPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        MCLogCore.onBlockPlace(context,(BlockItem)(Object)this);
    }

    @Inject(
            method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onBlockPlaceHead(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        this.setBeforeState(blockState);
        this.setBeforeFluidState(world.getFluidState(blockPos));

    }

    @Override
    public void setBeforeState(BlockState state) {
        this.beforeState = state;
    }

    @Override
    public BlockState getBeforeState() {
        return this.beforeState;
    }

    @Override
    public void setBeforeFluidState(FluidState state) {
        this.fluidState = state;
    }

    @Override
    public FluidState getBeforeFluidState() {
        return this.fluidState;
    }
}
