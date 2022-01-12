package cn.acyco.mclog.ext;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/**
 * @author Acyco
 * @create 2022-01-12 16:20
 * @url https://acyco.cn
 */
public class BucketItemBeforeExt {
    private BlockPos blockPos = null;
    private BlockState blockState = null;

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public void setBlockState(BlockState blockState) {
        this.blockState = blockState;
    }

}
