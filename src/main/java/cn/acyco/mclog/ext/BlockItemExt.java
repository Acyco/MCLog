package cn.acyco.mclog.ext;

import net.minecraft.block.BlockState;

/**
 * @author Acyco
 * @create 2022-01-03 20:01
 * @url https://acyco.cn
 */
public interface BlockItemExt {
    void setBeforeState(BlockState state);
    BlockState getBeforeState();
}
