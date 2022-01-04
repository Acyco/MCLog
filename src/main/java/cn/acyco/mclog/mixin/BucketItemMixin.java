package cn.acyco.mclog.mixin;

import cn.acyco.mclog.MCLogCore;
import cn.acyco.mclog.ext.BucketItemExt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Acyco
 * @create 2022-01-04 15:58
 * @url https://acyco.cn
 */
@Mixin(BucketItem.class)
public abstract class BucketItemMixin implements BucketItemExt {

    @Shadow @Final private Fluid fluid;

    @Inject(method = "use", at = @At(
            value = "HEAD"
            //target= "Lnet/minecraft/util/TypedActionResult;success(Ljava/lang/Object;Z)Lnet/minecraft/util/TypedActionResult;"
    ))
    public void onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        MCLogCore.OnBucketUse((BucketItem) (Object) this, world, user, hand);
    }

    @Override
    public Fluid getFluid() {
        return  this.fluid;
    }
}
