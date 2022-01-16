package cn.acyco.mclog.mixin.items;

import cn.acyco.mclog.core.MCLogCore;
import net.minecraft.block.BlockState;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * '
 *
 * @author Acyco
 * @create 2022-01-17 01:40
 * @url https://acyco.cn
 */
@Mixin(HoneycombItem.class)
public abstract class HoneycombItemMixin {

    /*c	bty	net/minecraft/class_5953	net/minecraft/item/HoneycombItem
	f	Ljava/util/function/Supplier;	b	field_29561	WAXED_TO_UNWAXED_BLOCKS
	f	Ljava/util/function/Supplier;	a	field_29560	UNWAXED_TO_WAXED_BLOCKS
	m	(Lbwg;Lgh;Lcad;Lcoc;)Lavq;	a	method_34719	method_34719
		p	3			state
	m	(Lcoc;)Ljava/util/Optional;	b	method_34720	getWaxedState
		p	0			state
	m	()Lcom/google/common/collect/BiMap;	j	method_34723	method_34723
	m	()Lcom/google/common/collect/BiMap;	i	method_34722	method_34722
	m	(Lcoc;Lccy;)Lcoc;	a	method_34721	method_34721
		p	1			block
    * */

    @ModifyArgs(method = "method_34719",at=@At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"
    ))
    private static void logUseOnBlock(Args args, ItemUsageContext context, BlockPos pos, World world, BlockState state) {
        MCLogCore.shovelOrAxeOrHoneycombItemUserOnBlock(args, context);
    }
}
