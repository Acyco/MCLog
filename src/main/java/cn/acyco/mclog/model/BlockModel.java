package cn.acyco.mclog.model;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

/**
 * @author Acyco
 * @create 2022-01-25 23:43
 * @url https://acyco.cn
 */
public class BlockModel {
    private BlockPos blockPos;
    private BlockState blockState;
    private DefaultedList<ItemStack> inventory;

    public BlockModel() {
        this(null, null);
    }

    public BlockModel(BlockPos blockPos, BlockState blockState, DefaultedList<ItemStack> inventory) {
        this.blockPos = blockPos;
        this.blockState = blockState;
        this.inventory = inventory;
    }

    public BlockModel(BlockPos blockPos, BlockState blockState) {
        this(blockPos, blockState, null);
    }

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

    public DefaultedList<ItemStack> getInventory() {
        return inventory;
    }

    public void setInventory(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    @Override
    public boolean equals(Object obj) {
        return this.blockPos.equals(((BlockModel) obj).getBlockPos());
    }

    @Override
    public int hashCode() {
        return Objects.hash( blockPos);
    }
}
