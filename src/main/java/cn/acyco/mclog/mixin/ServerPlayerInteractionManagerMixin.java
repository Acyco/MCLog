package cn.acyco.mclog.mixin;

import cn.acyco.mclog.core.MCLogCore;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
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

/**
 * @author Acyco
 * @create 2022-01-01 15:51
 * @url https://acyco.cn
 */
@Mixin(value = ServerPlayerInteractionManager.class,priority = 233)
public abstract class ServerPlayerInteractionManagerMixin {
    @Shadow @Final protected ServerPlayerEntity player;

    @Shadow protected ServerWorld world;
    private DefaultedList<ItemStack> saveItemStack = null;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))

    private void onTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockEntity blockEntity = this.world.getBlockEntity(pos);
        if (blockEntity instanceof Inventory) {
            Inventory inventory = (Inventory) blockEntity;
            saveItemStack = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
            for (int i = 0; i < inventory.size(); i++) {
                saveItemStack.set(i, inventory.getStack(i).copy()); //一定要用copy() 才能保存
            }
        }
    }

    @Inject(method = "tryBreakBlock",
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;onBroken(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void onBlockBroken(BlockPos pos, CallbackInfoReturnable<Boolean> cir, BlockState blockState, BlockEntity blockEntity, Block block, boolean bl) {
        MCLogCore.onBlockBroken(this.saveItemStack,player, pos, blockState,this.world);
        this.saveItemStack = null;
    }

    @Inject(method = "interactBlock",require = 0,cancellable = true, at = @At("RETURN"))
    private void onInteractBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir)
    {
        MCLogCore.onInteractBlock(player, world, stack, hand, hitResult,cir);
    }

}
