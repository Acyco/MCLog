package cn.acyco.mclog.ext;

import net.minecraft.block.BlockState;

/**
 * @author Acyco
 * @create 2022-01-14 19:22
 * @url https://acyco.cn
 */
public interface AbstractBlockStateExt {
    public BlockState getBeforeBlockState();
    public void setBeforeBlockState(BlockState blockState);
}
