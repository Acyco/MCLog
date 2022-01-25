package cn.acyco.mclog.utils;

import cn.acyco.mclog.enums.TrackDirection;
import cn.acyco.mclog.model.BlockModel;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedHashSet;

/**
 * @author Acyco
 * @create 2022-01-26 01:10
 * @url https://acyco.cn
 */
public class TrackBlockUtil {
    public static void trackUp(ServerWorld world, LinkedHashSet<BlockModel> trackBlocks, final BlockPos pos) {
        trackUp(world, trackBlocks, pos, 384);
    }

    public static void trackUp(ServerWorld world, LinkedHashSet<BlockModel> trackBlocks, final BlockPos pos, int num) {
        track(world, trackBlocks, pos, TrackDirection.UP, num);
    }

    public static void trackDown(ServerWorld world, LinkedHashSet<BlockModel> trackBlocks, final BlockPos pos) {
        trackDown(world, trackBlocks, pos, 384);
    }

    public static void trackDown(ServerWorld world, LinkedHashSet<BlockModel> trackBlocks, final BlockPos pos, int num) {
        track(world, trackBlocks, pos, TrackDirection.DOWN, num);
    }
    public static void trackUpDown(ServerWorld world, LinkedHashSet<BlockModel> trackBlocks, final BlockPos pos) {
        trackUPDown(world, trackBlocks, pos, 384);
    }

    public static void trackUPDown(ServerWorld world, LinkedHashSet<BlockModel> trackBlocks, final BlockPos pos, int num) {
        track(world, trackBlocks, pos, TrackDirection.UP, num);
        track(world, trackBlocks, pos, TrackDirection.DOWN, num);
    }

    public static void trackEast(ServerWorld world, LinkedHashSet<BlockModel> trackBlocks, final BlockPos pos) {
        trackEast(world, trackBlocks, pos, 384);
    }

    public static void trackEast(ServerWorld world, LinkedHashSet<BlockModel> trackBlocks, final BlockPos pos, int num) {
        track(world, trackBlocks, pos, TrackDirection.EAST, num);
    }

    public static void trackWest(ServerWorld world, LinkedHashSet<BlockModel> trackBlocks, final BlockPos pos) {
        trackWest(world, trackBlocks, pos, 256);
    }

    public static void trackWest(ServerWorld world, LinkedHashSet<BlockModel> trackBlocks, final BlockPos pos, int num) {
        track(world, trackBlocks, pos, TrackDirection.WEST, num);
    }

    public static void trackSouth(ServerWorld world, LinkedHashSet<BlockModel> trackBlocks, final BlockPos pos) {
        trackSouth(world, trackBlocks, pos, 256);
    }

    public static void trackSouth(ServerWorld world, LinkedHashSet<BlockModel> trackBlocks, final BlockPos pos, int num) {
        track(world, trackBlocks, pos, TrackDirection.SOUTH, num);
    }

    public static void trackNorth(ServerWorld world, LinkedHashSet<BlockModel> trackBlocks, final BlockPos pos) {
        trackNorth(world, trackBlocks, pos, 256);
    }

    public static void trackNorth(ServerWorld world, LinkedHashSet<BlockModel> trackBlocks, final BlockPos pos, int num) {
        track(world, trackBlocks, pos, TrackDirection.NORTH, num);
    }

    private static void track(ServerWorld world, LinkedHashSet<BlockModel> trackBlocks, final BlockPos pos, TrackDirection trackDirection, int num) {
        BlockPos blockPos = pos;
        BlockState upBlockState;
        int count = 0;
        while (!((upBlockState = world.getBlockState(blockPos)).getBlock().equals(Blocks.AIR)) && count < num) {
            BlockModel model = new BlockModel();
            model.setBlockPos(blockPos);
            model.setBlockState(upBlockState);
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            if (blockEntity instanceof Inventory inventory) {
                DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
                for (int i = 0; i < inventory.size(); i++) {
                    itemStacks.set(i, inventory.getStack(i).copy()); //一定要用copy() 才能保存
                }
                model.setInventory(itemStacks);
            }


            switch (trackDirection) {
                case UP -> blockPos = blockPos.up();
                case DOWN -> blockPos = blockPos.down();
                case EAST -> blockPos = blockPos.east();
                case WEST -> blockPos = blockPos.west();
                case SOUTH -> blockPos = blockPos.south();
                case NORTH -> blockPos = blockPos.north();
            }
            trackBlocks.add(model);
            System.out.println(trackDirection);
           /* if (trackDirection != TrackDirection.UP || trackDirection != TrackDirection.DOWN) {
                System.out.println("");
            }*/


            count++;
        }
    }
}
