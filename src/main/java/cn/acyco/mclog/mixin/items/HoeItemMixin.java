package cn.acyco.mclog.mixin.items;

import cn.acyco.mclog.core.MCLogCore;
import net.minecraft.block.BlockState;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemUsageContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Acyco
 * @create 2022-01-16 04:39
 * @url https://acyco.cn
 */
@Mixin(HoeItem.class)
public abstract class HoeItemMixin {
    /*
    	m	(Lcoc;)Ljava/util/function/Consumer;	b	method_36988	createTillAction
		c	{@return a tilling action that sets a block state}
		p	0			result
			c	the tilled block state
	m	(Lbwg;)Z	c	method_36989	method_36989
	m	(Lcoc;Lbwg;)V	a	method_36984	method_36984
		p	1			context
	m	(Lcoc;Lcac;Lbwg;)V	a	method_36986	method_36986
		p	2			context
	m	(Lcoc;Lcac;)Ljava/util/function/Consumer;	a	method_36985	createTillAndDropAction
		c	{@return a tilling action that sets a block state and drops an item}
		p	1			droppedItem
			c	the item to drop
		p	0			result
	e		c	the tilled block state
     */
    @SuppressWarnings("all")
    // @Inject(method = "method_36984", at = @At("HEAD"))

    private static void logCreateTillAction(BlockState blockState, ItemUsageContext context, CallbackInfo ci) {
        MCLogCore.hoeItemUserOnBlock(blockState,context);

    }

    @SuppressWarnings("all")
     @Inject(method = "method_36986", at = @At("HEAD"))
    private static void logCreateTillAndDropAction(BlockState blockState, ItemConvertible itemConvertible, ItemUsageContext context, CallbackInfo ci) {
        //rooted dirt
        MCLogCore.hoeItemUserOnBlock(blockState, context);
    }

    //createTillAction
}
