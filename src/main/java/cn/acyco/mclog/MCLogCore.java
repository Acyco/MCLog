package cn.acyco.mclog;

import cn.acyco.mclog.config.Config;
import cn.acyco.mclog.database.SqliteHelper;
import cn.acyco.mclog.ext.BlockItemExt;
import cn.acyco.mclog.ext.BucketItemExt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.RaycastContext;
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
            BLOCK_MAP = SqliteHelper.getDataMap(SqliteHelper.tableNameBlockMap, "item");
        }
        return BLOCK_MAP.get(key);
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
        SqliteHelper.insert(SqliteHelper.tableNameDeath, map);
        for (ItemStack itemStack : player.getInventory().main) {
            if (itemStack.getItem() != Items.AIR) {
                System.out.println(itemStack.getItem().getTranslationKey());
                System.out.println(itemStack.getCount());

            }
        }

    }


    /**
     * 方块破坏事件
     *
     * @param player 玩家
     * @param pos 方块位置
     * @param blockState 方块状态
     * @param world 世界
     */
    public static void onBlockBroken(ServerPlayerEntity player, BlockPos pos, BlockState blockState, ServerWorld world) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("time", (int) (new Date().getTime() / 1000));
        map.put("uid", getUserId(player));
        map.put("wid", getWorldId(world));
        map.put("x", pos.getX());
        map.put("y", pos.getY());
        map.put("z", pos.getZ());
        map.put("bid", getBlockId(blockState.getBlock()));
        map.put("sid", getBlockStateId(blockState));
        map.put("action", 0); //1 broken
        map.put("rolled_back", 0);
        SqliteHelper.insert(SqliteHelper.tableNameBlock, map);

    }


    /**
     * 方块放置事件
     *
     * @param context 上下文
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
                if (!beforeState.getFluidState().isEmpty() && !isWaterlogged(blockState)) { // 放置前的方块是流体，放置后不是含水，
                    //System.out.println("remove fluid");
                    onBlockBroken((ServerPlayerEntity) player, blockPos, beforeState, (ServerWorld) context.getWorld());
                    blockItemExt.setBeforeState(null); //处理完重置为null
                }
            }


            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("time", (int) (new Date().getTime() / 1000));
            map.put("uid", getUserId(player));
            map.put("wid", getWorldId(context.getWorld()));
            map.put("x", blockPos.getX());
            map.put("y", blockPos.getY());
            map.put("z", blockPos.getZ());
            map.put("bid", getBlockId(blockState.getBlock()));
            map.put("sid", getBlockStateId(blockState));
            map.put("action", 1); //1 placed
            map.put("rolled_back", 0);
            SqliteHelper.insert(SqliteHelper.tableNameBlock, map);

        }

    }


    /**
     * 玩家登录
     *
     * @param player 玩家
     */
    public static void onPlayerConnect(ServerPlayerEntity player) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("time", (int) (new Date().getTime() / 1000));
        map.put("uid", getUserId(player));
        map.put("wid", getWorldId(player.getWorld()));
        map.put("x", player.getBlockX());
        map.put("y", player.getBlockY());
        map.put("z", player.getBlockZ());
        map.put("action", 1);

        SqliteHelper.insert(SqliteHelper.tableNameSesssion, map);
    }

    /**
     * 玩家离线
     *
     * @param player 玩家
     */
    public static void onDisconnect(ServerPlayerEntity player) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("time", (int) (new Date().getTime() / 1000));
        map.put("uid", getUserId(player));
        map.put("wid", getWorldId(player.getWorld()));
        map.put("x", player.getBlockX());
        map.put("y", player.getBlockY());
        map.put("z", player.getBlockZ());
        map.put("action", 0);
        SqliteHelper.insert(SqliteHelper.tableNameSesssion, map);
    }

    private static BlockHitResult raycast(World world, PlayerEntity player, RaycastContext.FluidHandling fluidHandling) {
        float f = player.getPitch();
        float g = player.getYaw();
        Vec3d vec3d = player.getEyePos();
        float h = MathHelper.cos(-g * ((float) Math.PI / 180) - (float) Math.PI);
        float i = MathHelper.sin(-g * ((float) Math.PI / 180) - (float) Math.PI);
        float j = -MathHelper.cos(-f * ((float) Math.PI / 180));
        float k = MathHelper.sin(-f * ((float) Math.PI / 180));
        float l = i * j;
        float n = h * j;
        double d = 5.0;
        Vec3d vec3d2 = vec3d.add((double) l * d, (double) k * d, (double) n * d);
        return world.raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.OUTLINE, fluidHandling, player));
    }

    public static void OnBucketUse(BucketItem bucketItem, World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            BucketItemExt bucketItemExt = (BucketItemExt) bucketItem;
            Fluid fluid = bucketItemExt.getFluid();
            ItemStack itemStack = user.getStackInHand(hand);
            RaycastContext.FluidHandling fluidHandling = fluid == Fluids.EMPTY ? RaycastContext.FluidHandling.SOURCE_ONLY : RaycastContext.FluidHandling.NONE;
            BlockHitResult blockHitResult = raycast(world, user, fluidHandling);
            if (blockHitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos fluidDrainable;
                BlockPos blockPos = blockHitResult.getBlockPos();

                BlockState blockState = world.getBlockState(blockPos);
                System.out.println(blockState);
                System.out.println("use");
                if (fluid == Fluids.EMPTY) {
                    //如果是空桶
                    System.out.println("empty");
                    FluidDrainable fluidDrainable2;
                    ItemStack itemStack2;
                    if (blockState.getBlock() instanceof FluidDrainable && !(itemStack2 = (fluidDrainable2 = (FluidDrainable) ((Object) blockState.getBlock())).tryDrainFluid(world, blockPos, blockState)).isEmpty()) {
                        System.out.println("dkdkdk");
                    }
                } else {
                    //。。。
                }
            }
            System.out.println(fluid);

            bucketItemExt.getFluid().getDefaultState();
        }

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

        //
    }

    public static void insertContainer(BlockPos blockPos, ServerPlayerEntity player, ItemStack itemStack, int action) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("time", (int) (new Date().getTime() / 1000));
        map.put("uid", getUserId(player));
        map.put("wid", getWorldId(player.getWorld()));
        map.put("x",blockPos.getX());
        map.put("y", blockPos.getY());
        map.put("z", blockPos.getZ());
        map.put("action", action);
        System.out.println(itemStack.writeNbt(new NbtCompound()));

       // SqliteHelper.insert(SqliteHelper.tableNameSesssion, map);
    }
}
