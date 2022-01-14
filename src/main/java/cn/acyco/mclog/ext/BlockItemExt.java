package cn.acyco.mclog.ext;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;

/**
 * @author Acyco
 * @create 2022-01-03 20:01
 * @url https://acyco.cn
 */
public interface BlockItemExt {
    void setBeforeState(BlockState state);
    BlockState getBeforeState();
    void setBeforeFluidState(FluidState state);
    FluidState getBeforeFluidState();
}
