package cn.acyco.mclog.core;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.Registry;

import java.util.HashSet;

/**
 * @author Acyco
 * @create 2022-01-14 18:22
 * @url https://acyco.cn
 */
public class InteractionBlockGroup {
    public static HashSet<String> BUTTON = new HashSet<>();

    public static void init() {
        //button
        BUTTON.add(register(Blocks.STONE_BUTTON));
        BUTTON.add(register(Blocks.OAK_BUTTON));
        BUTTON.add(register(Blocks.SPRUCE_BUTTON));
        BUTTON.add(register(Blocks.BIRCH_BUTTON));
        BUTTON.add(register(Blocks.JUNGLE_BUTTON));
        BUTTON.add(register(Blocks.ACACIA_BUTTON));
        BUTTON.add(register(Blocks.DARK_OAK_BUTTON));
        BUTTON.add(register(Blocks.CRIMSON_BUTTON));
        BUTTON.add(register(Blocks.WARPED_BUTTON));
        BUTTON.add(register(Blocks.POLISHED_BLACKSTONE_BUTTON));
        //redstone

    }

    public static String register(Block block) {

        return Registry.BLOCK.getId(block).toString();
    }
}
