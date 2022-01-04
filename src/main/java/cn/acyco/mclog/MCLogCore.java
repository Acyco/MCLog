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
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    public static HashMap<String, Integer> BLOCKMAP = null;
    public static HashMap<String, Integer> BLOCKSTATEMAP = null;

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
        BLOCKMAP = SqliteHelper.getDataMap(SqliteHelper.tableNameBlockMap, "block");
        BLOCKSTATEMAP = SqliteHelper.getDataMap(SqliteHelper.tableNameBlockStateMap, "state");
    }

    public static void serverLoadedWorlds(MinecraftServer minecraftServer) {
        for (ServerWorld world : server.getWorlds()) {
            String worldStr = world.getRegistryKey().getValue().toString();
            insertWorlds(worldStr);
        }
    }

    public static File getPathFile(String name) {
        return server.getSavePath(WorldSavePath.ROOT).resolve(name).toFile();
    }


    /**
     * 获取维度（世界）id
     *
     * @param world 世界
     * @return
     */
    public static int getWorldId(World world) {
        String worldStr = world.getRegistryKey().getValue().toString();
        insertWorlds(worldStr);
        return WORLDS.get(worldStr);
    }

    private static void insertWorlds(String worldStr) {
        if (!WORLDS.containsKey(worldStr)) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("world", worldStr);
            SqliteHelper.insert(SqliteHelper.tableNameWorld, map);
            WORLDS = SqliteHelper.getDataMap(SqliteHelper.tableNameWorld, "world");
        }
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
/*
    private static int getUserId(ServerPlayerEntity player) {
        String uuid = String.valueOf(player.getUuid());
        String user = player.getName().getString();
        return getUserId(user, uuid);
    }*/

    private static int getUserId(PlayerEntity player) {
        String uuid = String.valueOf(player.getUuid());
        String user = player.getName().getString();
        return getUserId(user, uuid);
    }

    public static int getBlockId(Block block) {
        String key = Registry.BLOCK.getId(block).toString();
        if (!BLOCKMAP.containsKey(key)) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("block", key);
            SqliteHelper.insert(SqliteHelper.tableNameBlockMap, map);
            //SqliteHelper.insertBlockMap(key);
            BLOCKMAP = SqliteHelper.getDataMap(SqliteHelper.tableNameBlockMap, "block");
        }
        return BLOCKMAP.get(key);
    }

    public static String getBlockStateId(BlockState blockState) {

        int size = blockState.getEntries().entrySet().size();
        int current = 0;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Property<?>, Comparable<?>> entry : blockState.getEntries().entrySet()) {
            String key = entry.getKey().getName() + "=" + entry.getValue();
            if (!BLOCKSTATEMAP.containsKey(key)) {
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                map.put("state", key);
                SqliteHelper.insert(SqliteHelper.tableNameBlockStateMap, map);
                BLOCKSTATEMAP = SqliteHelper.getDataMap(SqliteHelper.tableNameBlockStateMap, "state");
            }
            sb.append(BLOCKSTATEMAP.get(key));
            current++;
            if (size > current) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * 死亡事件
     *
     * @param source
     * @param ci
     * @param player
     */
    public static void onDeath(DamageSource source, CallbackInfo ci, ServerPlayerEntity player) {

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
            System.out.println(itemStack);
        }
    }


    /**
     * 方块破坏事件
     *
     * @param player
     * @param pos
     * @param blockState
     * @param world
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

    public static boolean isWaterlogged(BlockState blockState) {
        for (Map.Entry<Property<?>, Comparable<?>> entry : blockState.getEntries().entrySet()) {
            //System.out.println(entry.getKey().getName()+',' + entry.getValue());
            if (entry.getKey().getName().equals("waterlogged") && entry.getValue().toString().equals("true")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 方块放置事件
     *
     * @param context
     * @param blockItem
     */
    public static void onBlockPlace(ItemPlacementContext context, BlockItem blockItem) {
        if (!context.getWorld().isClient) {
            BlockPos blockPos = context.getBlockPos();
            BlockState blockState = context.getWorld().getBlockState(blockPos);

            BlockItemExt blockItemExt = (BlockItemExt) blockItem;
            BlockState beforeState = blockItemExt.getBeforeState();
            //System.out.println(beforeState.getFluidState().getFluid());
            if (!beforeState.getFluidState().isEmpty() && !isWaterlogged(blockState)) { // 放置前的方块是流体，放置后不是含水，
                //System.out.println("remove fluid");
                onBlockBroken((ServerPlayerEntity) context.getPlayer(), blockPos, beforeState, (ServerWorld) context.getWorld());
            }


            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("time", (int) (new Date().getTime() / 1000));
            map.put("uid", getUserId(context.getPlayer()));
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
     * @param connection
     * @param player
     */
    public static void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player) {
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
     * @param ci
     * @param player
     */
    public static void onDisconnect(CallbackInfo ci, ServerPlayerEntity player) {
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

    private static BlockHitResult raycast(Fluid fluid, World world, PlayerEntity player, RaycastContext.FluidHandling fluidHandling) {
        float f = player.getPitch();
        float g = player.getYaw();
        Vec3d vec3d = player.getEyePos();
        float h = MathHelper.cos(-g * ((float) Math.PI / 180) - (float) Math.PI);
        float i = MathHelper.sin(-g * ((float) Math.PI / 180) - (float) Math.PI);
        float j = -MathHelper.cos(-f * ((float) Math.PI / 180));
        float k = MathHelper.sin(-f * ((float) Math.PI / 180));
        float l = i * j;
        float m = k;
        float n = h * j;
        double d = 5.0;
        Vec3d vec3d2 = vec3d.add((double) l * 5.0, (double) m * 5.0, (double) n * 5.0);
        return world.raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.OUTLINE, fluid == Fluids.EMPTY ? RaycastContext.FluidHandling.SOURCE_ONLY : RaycastContext.FluidHandling.NONE, player));
    }

    public static void OnBucketUse(BucketItem bucketItem, World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            BucketItemExt bucketItemExt = (BucketItemExt) bucketItem;
            Fluid fluid = bucketItemExt.getFluid();
            ItemStack itemStack = user.getStackInHand(hand);
            BlockHitResult blockHitResult = raycast(fluid, world, user, fluid == Fluids.EMPTY ? RaycastContext.FluidHandling.SOURCE_ONLY : RaycastContext.FluidHandling.NONE);
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
                    if (blockState.getBlock() instanceof FluidDrainable && !(itemStack2 = (fluidDrainable2 = (FluidDrainable)((Object)blockState.getBlock())).tryDrainFluid(world, blockPos, blockState)).isEmpty()) {
                        System.out.println("dkdkdk");
                    }
                } else {

                }
            }
            System.out.println(fluid);

            bucketItemExt.getFluid().getDefaultState();
        }

    }
}
