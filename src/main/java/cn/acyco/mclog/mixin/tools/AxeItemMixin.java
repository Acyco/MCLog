package cn.acyco.mclog.mixin.tools;

import cn.acyco.mclog.core.MCLogCore;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemUsageContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * @author Acyco
 * @create 2022-01-15 05:57
 * @url https://acyco.cn
 */
@Mixin(AxeItem.class)
public abstract class AxeItemMixin {
    @ModifyArgs(method = "useOnBlock",at=@At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"
    ))
    public void logUseOnBlock(Args args, ItemUsageContext context) {
        MCLogCore.shovelOrAxeItemUserOnBlock(args, context);
    }
}
