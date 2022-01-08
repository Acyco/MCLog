package cn.acyco.mclog.mixin;

import cn.acyco.mclog.ext.ScreenHandlerExt;
import cn.acyco.mclog.ext.SlotExt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Acyco
 * @create 2022-01-06 23:10
 * @url https://acyco.cn
 */
@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin implements ScreenHandlerExt {
    @Unique
    private ServerPlayerEntity serverPlayer = null;


    @Inject(method = "onButtonClick", at = @At("HEAD"))
    public void onButtonClick(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!player.getWorld().isClient) {
            this.serverPlayer = (ServerPlayerEntity) player;
        }
    }


    @Inject(method = "transferSlot", at = @At("HEAD"))
    public void transferSlot(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!player.getWorld().isClient) {
            this.serverPlayer = (ServerPlayerEntity) player;
        }
    }

    @Inject(method = "onSlotClick", at = @At("HEAD"))
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (!player.getWorld().isClient) {
            this.serverPlayer = (ServerPlayerEntity) player;
        }
    }

    @Inject(method = "dropInventory", at = @At("HEAD"))
    public void dropInventory(PlayerEntity player, Inventory inventory, CallbackInfo ci) {
        if (!player.getWorld().isClient) {
            this.serverPlayer = (ServerPlayerEntity) player;
        }
    }


    @Override
    @Nullable
    public ServerPlayerEntity getServerPlayer() {
        return this.serverPlayer;
    }


    @Inject(method="addSlot" ,at=@At("HEAD")   )
    public void addSlot(Slot slot, CallbackInfoReturnable<Slot> cir) {
        ((SlotExt)slot).setScreenHandler((ScreenHandler)(Object) this);
    }
}
