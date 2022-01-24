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
public class TrackBlock {
    public static HashSet<String> BUTTON = new HashSet<>();
    public static HashSet<String> BED = new HashSet<>();



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
        //bed
        BED.add(register(Blocks.BLACK_BED));
        BED.add(register(Blocks.BLUE_BED));
        BED.add(register(Blocks.BROWN_BED));
        BED.add(register(Blocks.CYAN_BED));
        BED.add(register(Blocks.GRAY_BED));
        BED.add(register(Blocks.GREEN_BED));
        BED.add(register(Blocks.LIGHT_BLUE_BED));
        BED.add(register(Blocks.LIGHT_GRAY_BED));
        BED.add(register(Blocks.LIME_BED));
        BED.add(register(Blocks.MAGENTA_BED));
        BED.add(register(Blocks.ORANGE_BED));
        BED.add(register(Blocks.PINK_BED));
        BED.add(register(Blocks.PURPLE_BED));
        BED.add(register(Blocks.RED_BED));
        BED.add(register(Blocks.WHITE_BED));
        BED.add(register(Blocks.YELLOW_BED));


    }

    public static String register(Block block) {

        return Registry.BLOCK.getId(block).toString();
    }

    public static boolean contains(HashSet<String> blockSet, Block block) {
        if (null == blockSet) {
            return false;
        }
        return blockSet.contains(register(block));
    }
}
