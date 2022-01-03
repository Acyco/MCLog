package cn.acyco.mclog;

import cn.acyco.mclog.config.Config;
import cn.acyco.mclog.database.SqliteHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
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
    public static HashMap<String,Integer> WORLDS = null;
    public static HashMap<String,Integer> USERS = null;
    public static HashMap<String,Integer> BLOCKMAP = null;
    public static HashMap<String,Integer> BLOCKSTATEMAP = null;

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
        WORLDS = SqliteHelper.getDataMap(SqliteHelper.tableNameWorld,"world");
        USERS = SqliteHelper.getDataMap(SqliteHelper.tableNameUser,"user");
        BLOCKMAP = SqliteHelper.getDataMap(SqliteHelper.tableNameBlockMap,"block");
        BLOCKSTATEMAP = SqliteHelper.getDataMap(SqliteHelper.tableNameBlockStateMap,"state");
    }

    public static File getPathFile(String name) {
        return server.getSavePath(WorldSavePath.ROOT).resolve(name).toFile();
    }


    /**
     * 获取维度（世界）id
     * @param world 世界
     * @return
     */
    public static int getWorldId(World world) {
        String worldStr = world.getRegistryKey().getValue().toString();

        if (!WORLDS.containsKey(worldStr)) {
            LinkedHashMap<String,Object> map =  new LinkedHashMap<>();
            map.put("world", worldStr);
            SqliteHelper.insert(SqliteHelper.tableNameWorld,map);
            WORLDS = SqliteHelper.getDataMap(SqliteHelper.tableNameWorld,"world");
        }
        return WORLDS.get(worldStr);
    }

    public static int getUserId(String user, String uuid) {
        if (!USERS.containsKey(user)) {
            LinkedHashMap<String,Object> map =  new LinkedHashMap<>();
            map.put("user", user);
            map.put("uuid", uuid);
            SqliteHelper.insert(SqliteHelper.tableNameUser,map);
            USERS = SqliteHelper.getDataMap(SqliteHelper.tableNameUser,"user");
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
           LinkedHashMap<String,Object> map =  new LinkedHashMap<>();
            map.put("block", key);
            SqliteHelper.insert(SqliteHelper.tableNameBlockMap,map);
            //SqliteHelper.insertBlockMap(key);
            BLOCKMAP = SqliteHelper.getDataMap(SqliteHelper.tableNameBlockMap,"block");
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
                SqliteHelper.insert(SqliteHelper.tableNameBlockStateMap,map);
                BLOCKSTATEMAP = SqliteHelper.getDataMap(SqliteHelper.tableNameBlockStateMap,"state");
            }
            sb.append(BLOCKSTATEMAP.get(key));
            current++;
            if (size > current ) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * 死亡事件
     * @param source
     * @param ci
     * @param player
     */
    public static void onDeath(DamageSource source, CallbackInfo ci, ServerPlayerEntity player) {

        LinkedHashMap<String,Object> map =  new LinkedHashMap<>();
        map.put("time", (int) (new Date().getTime() / 1000));
        map.put("uid", getUserId(player));
        map.put("wid", getWorldId(player.getWorld()));
        map.put("x", player.getBlockX() );
        map.put("y",  player.getBlockY());
        map.put("z",  player.getBlockZ());
        map.put("msg",  player.getDamageTracker().getDeathMessage().getString());
        map.put("attacker",   source.getAttacker() == null ? "":source.getAttacker().toString());

        SqliteHelper.insert(SqliteHelper.tableNameDeath,map);
    }


    /**
     * 方块破坏事件
     * @param player
     * @param pos
     * @param blockState
     * @param world
     */
    public static void onBlockBroken(ServerPlayerEntity player, BlockPos pos, BlockState blockState, ServerWorld world) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("time", (int) (new Date().getTime() / 1000));
        map.put("uid", getUserId(player));
        map.put("wid",getWorldId(world) );
        map.put("x",pos.getX() );
        map.put("y",pos.getY() );
        map.put("z",pos.getZ() );
        map.put("bid",getBlockId(blockState.getBlock()) );
        map.put("sid",getBlockStateId(blockState) );
        map.put("action", 0); //1 broken
        map.put("rolled_back",0);
        SqliteHelper.insert(SqliteHelper.tableNameBlock, map);

    }

    /**
     * 方块放置事件
     * @param context
     */
    public static void onBlockPlace(ItemPlacementContext context) {
        if (!context.getWorld().isClient) {
            BlockPos blockPos = context.getBlockPos();
            BlockState blockState = context.getWorld().getBlockState(blockPos);
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("time", (int) (new Date().getTime() / 1000));
            map.put("uid", getUserId(context.getPlayer()));
            map.put("wid",getWorldId(context.getWorld()) );
            map.put("x",blockPos.getX() );
            map.put("y",blockPos.getY() );
            map.put("z",blockPos.getZ() );
            map.put("bid",getBlockId(blockState.getBlock()) );
            map.put("sid",getBlockStateId(blockState) );
            map.put("action", 1); //1 placed
            map.put("rolled_back",0);
            SqliteHelper.insert(SqliteHelper.tableNameBlock, map);

        }

    }


    /**
     * 玩家登录
     * @param connection
     * @param player
     */
    public static void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player) {
    LinkedHashMap<String,Object> map =  new LinkedHashMap<>();
        map.put("time", (int) (new Date().getTime() / 1000));
        map.put("uid", getUserId(player));
        map.put("wid", getWorldId(player.getWorld()));
        map.put("x", player.getBlockX() );
        map.put("y",  player.getBlockY());
        map.put("z",  player.getBlockZ());
        map.put("action", 1);

        SqliteHelper.insert(SqliteHelper.tableNameSesssion,map);
    }

    /**
     * 玩家离线
     * @param ci
     * @param player
     */
    public static void onDisconnect(CallbackInfo ci, ServerPlayerEntity player) {
     LinkedHashMap<String,Object> map =  new LinkedHashMap<>();
        map.put("time", (int) (new Date().getTime() / 1000));
        map.put("uid", getUserId(player));
        map.put("wid", getWorldId(player.getWorld()));
        map.put("x", player.getBlockX() );
        map.put("y",  player.getBlockY());
        map.put("z",  player.getBlockZ());
        map.put("action", 0);
        SqliteHelper.insert(SqliteHelper.tableNameSesssion,map);
    }
}
