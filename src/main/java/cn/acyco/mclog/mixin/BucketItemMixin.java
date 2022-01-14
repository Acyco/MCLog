package cn.acyco.mclog.mixin;

import cn.acyco.mclog.core.BlockActionType;
import cn.acyco.mclog.core.MCLogCore;
import cn.acyco.mclog.ext.BucketItemBeforeExt;
import cn.acyco.mclog.ext.BucketItemExt;
import cn.acyco.mclog.utils.BlockHitResultUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
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

    @Shadow
    @Final
    private Fluid fluid;
    private BucketItemBeforeExt bucketItemBeforeExt = new BucketItemBeforeExt();

    @Inject(method = "use", at = @At(
            value = "HEAD"
    ))
    public void onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack itemStack = user.getStackInHand(hand);

        BlockHitResult blockHitResult = BlockHitResultUtil.raycast(world, user, this.fluid == Fluids.EMPTY ? RaycastContext.FluidHandling.SOURCE_ONLY : RaycastContext.FluidHandling.NONE);
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return;
        }
        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getSide();
            BlockPos blockPos2 = blockPos.offset(direction);
            if (this.fluid == Fluids.EMPTY) {
                bucketItemBeforeExt.setBlockPos(blockPos);
                bucketItemBeforeExt.setBlockState(world.getFluidState(blockPos).getBlockState());
                if (!world.canPlayerModifyAt(user, blockPos) || !user.canPlaceOn(blockPos2, direction, itemStack)) {
                    return;
                }
                return;
            }
            BlockState blockState = world.getBlockState(blockPos);
            BlockPos blockPos3 = blockState.getBlock() instanceof FluidFillable && this.fluid == Fluids.WATER ? blockPos : blockPos2;
            bucketItemBeforeExt.setBlockPos(blockPos3);
            bucketItemBeforeExt.setBlockState(world.getBlockState(blockPos3));
        }
    }

    @Override
    public Fluid getFluid() {
        return this.fluid;
    }

    @Inject(method = "use", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/TypedActionResult;success(Ljava/lang/Object;Z)Lnet/minecraft/util/TypedActionResult;", ordinal = 0
    ))
    public void onUseEmpty(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        MCLogCore.onBucketUse(world, user, bucketItemBeforeExt, BlockActionType.BREAK);
    }

    @Inject(method = "use", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/TypedActionResult;success(Ljava/lang/Object;Z)Lnet/minecraft/util/TypedActionResult;", ordinal = 1
    ))
    public void onUsePlace(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {



        MCLogCore.onBucketUse(world, user, bucketItemBeforeExt, BlockActionType.PLACE);

    }


    @Inject(method = "placeFluid", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"
    ))
    public void place(PlayerEntity player, World world, BlockPos pos, BlockHitResult hitResult, CallbackInfoReturnable<Boolean> cir) {
        MCLogCore.placeFluid((BucketItem)(Object)this,player, world, pos, hitResult, cir);
    }
}
