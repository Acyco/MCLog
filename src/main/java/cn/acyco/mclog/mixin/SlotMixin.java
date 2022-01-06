package cn.acyco.mclog.mixin;

import cn.acyco.mclog.ext.SlotExt;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * @author Acyco
 * @create 2022-01-06 23:19
 * @url https://acyco.cn
 */
@Mixin(Slot.class)
public abstract class SlotMixin implements SlotExt {
    @Unique
    private ScreenHandler screenHandler = null;
    @Override
    public void setScreenHandler(ScreenHandler screenHandler) {
        this.screenHandler = screenHandler;
    }

    @Override
    public ScreenHandler getScreenHandler() {
        return this.screenHandler;
    }





}
