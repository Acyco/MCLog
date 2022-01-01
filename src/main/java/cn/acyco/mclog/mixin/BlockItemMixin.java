package cn.acyco.mclog.mixin;

import cn.acyco.mclog.MCLogCore;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
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
public class BlockItemMixin {

    @Inject(
            method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/ActionResult;success(Z)Lnet/minecraft/util/ActionResult;"
            ),
            cancellable = true
    )
    private void onBlockPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        MCLogCore.onBlockPlace(context);
    }
}
