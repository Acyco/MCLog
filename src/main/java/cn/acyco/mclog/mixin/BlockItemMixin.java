package cn.acyco.mclog.mixin;

import cn.acyco.mclog.core.MCLogCore;
import cn.acyco.mclog.ext.BlockItemExt;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
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
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = context.getWorld().getBlockState(blockPos);
        setBeforeState(blockState);

    }

    @Override
    public void setBeforeState(BlockState state) {
        this.beforeState = state;
    }

    @Override
    public BlockState getBeforeState() {
        return this.beforeState;
    }
}
