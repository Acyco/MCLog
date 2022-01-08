package cn.acyco.mclog.mixin;

import cn.acyco.mclog.MCLogCore;
import cn.acyco.mclog.ext.ScreenHandlerExt;
import cn.acyco.mclog.ext.SlotExt;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Acyco
 * @create 2022-01-06 23:19
 * @url https://acyco.cn
 */
@Mixin(Slot.class)
public abstract class SlotMixin implements SlotExt {
    @Shadow public abstract ItemStack getStack();

    @Shadow @Final public Inventory inventory;
    private ItemStack beforeItemStack = null;
    @Unique
    private ScreenHandler screenHandler = null;
    @Override
    public void setScreenHandler(ScreenHandler screenHandler) {
        this.beforeItemStack = this.getStack() == null ? ItemStack.EMPTY : this.getStack().copy();
        this.screenHandler = screenHandler;
    }

    @Override
    public ScreenHandler getScreenHandler() {
        return this.screenHandler;
    }


    @Inject(method = "markDirty", at = @At("HEAD"))
    public void markDirty(CallbackInfo ci) {
        ScreenHandlerExt screenHandlerExt = (ScreenHandlerExt) screenHandler;
        PlayerEntity player = screenHandlerExt.getServerPlayer();
        if(player == null) return;
        if (!player.getWorld().isClient) {
            Inventory inventory = this.inventory;
            BlockPos blockPos = null;
            if (inventory instanceof LockableContainerBlockEntity) {
                blockPos = ((LockableContainerBlockEntity) inventory).getPos();
            }
            if (blockPos != null) {
                MCLogCore.inventoryUpdate(player, beforeItemStack, this.getStack().copy(), blockPos);
            }
            beforeItemStack = this.getStack().copy();
        }
    }
}
