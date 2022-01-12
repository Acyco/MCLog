package cn.acyco.mclog;

import cn.acyco.mclog.config.Config;
import cn.acyco.mclog.database.SqliteHelper;
import cn.acyco.mclog.ext.BlockItemExt;
import cn.acyco.mclog.ext.BucketItemBeforeExt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Acyco
 * @create 2022-01-01 13:03
 * @url https://acyco.cn
 */
public class MCLogCore {
    public static MinecraftServer server;
    public static Config config;
    public static final Logger LOGGER = LogManager.getLogger("MCLog");
    public static HashMap<String, Integer> WORLDS = null;
    public static HashMap<String, Integer> USERS = null;
    public static HashMap<String, Integer> BLOCK_MAP = null;
    public static HashMap<String, Integer> BLOCK_STATE_MAP = null;
    public static HashMap<String, Integer> ITEM_MAP = null;

    public static void serverLoaded(MinecraftServer minecraftServer) {
        System.out.println("mc server loaded");
        try {
            server = minecraftServer;
            config = Config.loadConfig();
            SqliteHelper.createTables();
            init();
        } catch (Exception exception) {
            LOGGER.error("[MCLog] 配置加载失败");
        }
    }

    public static void init() {
        WORLDS = SqliteHelper.getDataMap(SqliteHelper.tableNameWorld, "world");
        USERS = SqliteHelper.getDataMap(SqliteHelper.tableNameUser, "user");
        BLOCK_MAP = SqliteHelper.getDataMap(SqliteHelper.tableNameBlockMap, "block");
        BLOCK_STATE_MAP = SqliteHelper.getDataMap(SqliteHelper.tableNameBlockStateMap, "state");
        ITEM_MAP = SqliteHelper.getDataMap(SqliteHelper.tableNameItemMap, "item");
    }

    public static void serverLoadedWorlds(MinecraftServer minecraftServer) {
        server = minecraftServer;
        for (ServerWorld world : server.getWorlds()) {
            String worldStr = world.getRegistryKey().getValue().toString();
            insertWorlds(worldStr);
        }
    }

    public static File getPathFile(String name) {
        return server.getSavePath(WorldSavePath.ROOT).resolve(name).toFile();
    }

    public static boolean isWaterlogged(BlockState blockState) {
        for (Map.Entry<Property<?>, Comparable<?>> entry : blockState.getEntries().entrySet()) {
            //System.out.println(entry.getKey().getName()+',' + entry.getValue());
            if (entry.getKey().getName().equals("waterlogged") && entry.getValue().toString().equals("true")) {
                return true;
            }
        }
        return false;
    }

    private static void insertWorlds(String worldStr) {
        if (!WORLDS.containsKey(worldStr)) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("world", worldStr);
            SqliteHelper.insert(SqliteHelper.tableNameWorld, map);
            WORLDS = SqliteHelper.getDataMap(SqliteHelper.tableNameWorld, "world");
        }
    }

    /**
     * 获取维度（世界）id
     *
     * @param world 世界
     * @return world id
     */
    public static int getWorldId(World world) {
        String worldStr = world.getRegistryKey().getValue().toString();
        insertWorlds(worldStr);
        return WORLDS.get(worldStr);
    }


    public static int getUserId(String user, String uuid) {
        if (!USERS.containsKey(user)) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("user", user);
            map.put("uuid", uuid);
            SqliteHelper.insert(SqliteHelper.tableNameUser, map);
            USERS = SqliteHelper.getDataMap(SqliteHelper.tableNameUser, "user");
        }
        return USERS.get(user);
    }

    private static int getUserId(@Nullable PlayerEntity player) {
        if (player == null) return 0;
        String uuid = String.valueOf(player.getUuid());
        String user = player.getName().getString();
        return getUserId(user, uuid);
    }

    public static int getBlockId(Block block) {
        String key = Registry.BLOCK.getId(block).toString();
        if (!BLOCK_MAP.containsKey(key)) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("block", key);
            SqliteHelper.insert(SqliteHelper.tableNameBlockMap, map);
            BLOCK_MAP = SqliteHelper.getDataMap(SqliteHelper.tableNameBlockMap, "block");
        }
        return BLOCK_MAP.get(key);
    }

    public static int getItemId(Item item) {
        String key = Registry.ITEM.getId(item).toString();
        if (!ITEM_MAP.containsKey(key)) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("item", key);
            SqliteHelper.insert(SqliteHelper.tableNameItemMap, map);
            ITEM_MAP = SqliteHelper.getDataMap(SqliteHelper.tableNameItemMap, "item");
        }
        return ITEM_MAP.get(key);
    }


    public static String getBlockStateId(BlockState blockState) {

        int size = blockState.getEntries().entrySet().size();
        int current = 0;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Property<?>, Comparable<?>> entry : blockState.getEntries().entrySet()) {
            String key = entry.getKey().getName() + "=" + entry.getValue();
            if (!BLOCK_STATE_MAP.containsKey(key)) {
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                map.put("state", key);
                SqliteHelper.insert(SqliteHelper.tableNameBlockStateMap, map);
                BLOCK_STATE_MAP = SqliteHelper.getDataMap(SqliteHelper.tableNameBlockStateMap, "state");
            }
            sb.append(BLOCK_STATE_MAP.get(key));
            current++;
            if (size > current) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * 玩家死亡事件
     *
     * @param source 伤害来源？
     * @param player 玩家
     */
    public static void onPlayerDeath(DamageSource source, ServerPlayerEntity player) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("time", (int) (new Date().getTime() / 1000));
        map.put("uid", getUserId(player));
        map.put("wid", getWorldId(player.getWorld()));
        map.put("x", player.getBlockX());
        map.put("y", player.getBlockY());
        map.put("z", player.getBlockZ());
        map.put("msg", player.getDamageTracker().getDeathMessage().getString());
        map.put("attacker", source.getAttacker() == null ? "" : source.getAttacker().toString());
        map.put("inventory", player.getInventory().writeNbt(new NbtList()).asString());
        SqliteHelper.insert(SqliteHelper.tableNameDeath, map);

    }

    public static void insertBlock(PlayerEntity player, BlockPos pos, BlockState blockState, World world, int action) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("time", (int) (new Date().getTime() / 1000));
        map.put("uid", getUserId(player));
        map.put("wid", getWorldId(world));
        map.put("x", pos.getX());
        map.put("y", pos.getY());
        map.put("z", pos.getZ());
        map.put("bid", getBlockId(blockState.getBlock()));
        map.put("sid", getBlockStateId(blockState));
        map.put("action", action);
        map.put("rolled_back", 0);
        SqliteHelper.insert(SqliteHelper.tableNameBlock, map);
    }

    public static void insertContainer(BlockPos blockPos, ServerPlayerEntity player, ItemStack itemStack, int action) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("time", (int) (new Date().getTime() / 1000));
        map.put("uid", getUserId(player));
        map.put("wid", getWorldId(player.getWorld()));
        map.put("x", blockPos.getX());
        map.put("y", blockPos.getY());
        map.put("z", blockPos.getZ());
        map.put("item", getItemId(itemStack.getItem()));
        map.put("data", itemStack.writeNbt(new NbtCompound()).asString());
        map.put("action", action);
        map.put("rolled_back", 0);

        SqliteHelper.insert(SqliteHelper.tableNameContainer, map);
    }

    public static void insertSession(ServerPlayerEntity player, int action) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("time", (int) (new Date().getTime() / 1000));
        map.put("uid", getUserId(player));
        map.put("wid", getWorldId(player.getWorld()));
        map.put("x", player.getBlockX());
        map.put("y", player.getBlockY());
        map.put("z", player.getBlockZ());
        map.put("action", action);

        SqliteHelper.insert(SqliteHelper.tableNameSesssion, map);
    }

    /**
     * 方块破坏事件
     *  @param saveItemStack
     * @param player     玩家
     * @param pos        方块位置
     * @param blockState 方块状态
     * @param world      世界
     */
    public static void onBlockBroken(DefaultedList<ItemStack> saveItemStack, ServerPlayerEntity player, BlockPos pos, BlockState blockState, ServerWorld world) {
        insertBlock(player, pos, blockState, world, 0);// action 0 broke
        if(saveItemStack == null) return;
            for (int i = 0; i < saveItemStack.size(); i++) {
                ItemStack itemStack = saveItemStack.get(i);
                if (!itemStack.isEmpty()) {
                    insertContainer( pos, player, itemStack,0);// action 0 remove
                    System.out.println(itemStack);
                }
            }
            //System.out.println(blockEntity.);

    }


    /**
     * 方块放置事件
     *
     * @param context   上下文
     * @param blockItem 手里的方块物品。。
     */
    public static void onBlockPlace(ItemPlacementContext context, BlockItem blockItem) {
        if (!context.getWorld().isClient) {
            BlockPos blockPos = context.getBlockPos();
            BlockState blockState = context.getWorld().getBlockState(blockPos);
            PlayerEntity player = context.getPlayer();
            BlockItemExt blockItemExt = (BlockItemExt) blockItem;
            BlockState beforeState = blockItemExt.getBeforeState();
            if (beforeState != null) {
                //System.out.println(beforeState.getFluidState().getFluid());
                if (!beforeState.getFluidState().isEmpty() && (blockState.contains(Properties.WATERLOGGED) && !blockState.get(Properties.WATERLOGGED))) { // 放置前的方块是流体(水或岩浆)，放置后不是含水，!isWaterlogged(blockState)
                    System.out.println("remove fluid");
                    onBlockBroken(null, (ServerPlayerEntity) player, blockPos, beforeState, (ServerWorld) context.getWorld());
                    blockItemExt.setBeforeState(null); //处理完重置为null
                }
            }
            insertBlock(player, blockPos, blockState, context.getWorld(), 1);// action 1 placed
        }
    }


    /**
     * 玩家登录
     *
     * @param player 玩家
     */
    public static void onPlayerConnect(ServerPlayerEntity player) {
        insertSession(player, 1);
    }

    /**
     * 玩家离线
     *
     * @param player 玩家
     */
    public static void onDisconnect(ServerPlayerEntity player) {
        insertSession(player, 0);
    }


    public static void inventoryUpdate(PlayerEntity player, ItemStack beforeItemStack, ItemStack afterItemStack, BlockPos blockPos) {
        if (beforeItemStack == null || afterItemStack == null) return;
        if (beforeItemStack.isEmpty() && afterItemStack.isEmpty()) return;


        if (!beforeItemStack.isEmpty() && !afterItemStack.isEmpty()) { //如果两者都是不空
            if (afterItemStack.getItem() == beforeItemStack.getItem()) { // 前后都是同一种物品

                int beforeCount = beforeItemStack.getCount();
                int afterCount = afterItemStack.getCount();

                if (afterCount < beforeCount) { //后面的数量比前面的还少 remove
                    inventoryUpdate(player, new ItemStack(afterItemStack.getItem(), beforeCount - afterCount), ItemStack.EMPTY, blockPos);

                } else {
                    //后面的数量比前面多
                    inventoryUpdate(player, ItemStack.EMPTY, new ItemStack(afterItemStack.getItem(), afterCount - beforeCount), blockPos);
                }
            } else {
                inventoryUpdate(player, ItemStack.EMPTY, afterItemStack, blockPos);
                inventoryUpdate(player, beforeItemStack, ItemStack.EMPTY, blockPos);
            }
            return;
        }

        boolean isBeforeEmpty = beforeItemStack.isEmpty();
        insertContainer(blockPos, (ServerPlayerEntity) player, isBeforeEmpty ? afterItemStack : beforeItemStack, isBeforeEmpty ? 1 : 0);
    }


    public static void onBucketUse(World world, PlayerEntity user, Hand hand, BucketItemBeforeExt bucketItemBeforeExt, int action) {
        if (world.isClient) {
            return;
        }
        insertBlock(user, bucketItemBeforeExt.getBlockPos(), bucketItemBeforeExt.getBlockState(), world, action);
    }


}
