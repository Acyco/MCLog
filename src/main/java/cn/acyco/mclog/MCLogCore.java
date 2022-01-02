package cn.acyco.mclog;

import cn.acyco.mclog.config.Config;
import cn.acyco.mclog.database.SqliteHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.naming.Name;
import java.io.File;
import java.util.HashMap;
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

    public static void serverLoaded(MinecraftServer minecraftServer) {
        System.out.println("mc server loaded");
        try {
            server = minecraftServer;
            config = Config.loadConfig();
            SqliteHelper.createTables();
            WORLDS = SqliteHelper.getWorldMap();
            USERS = SqliteHelper.getUserMap();
            SqliteHelper.getWorldMap();


        } catch (Exception exception) {
            LOGGER.error("[MCLog] 配置加载失败");
        }
    }

    public static File getPathFile(String name) {
        return server.getSavePath(WorldSavePath.ROOT).resolve(name).toFile();
    }



    public static int getWorldId(World world) {
        String worldStr = world.getRegistryKey().getValue().toString();
        SqliteHelper.insertWorld(worldStr);
        WORLDS = SqliteHelper.getWorldMap(); //插入完重置一下worlds
        //String worldStr = world.getRegistryKey().getValue().toString();
        if (WORLDS.containsKey(worldStr)) {
           return WORLDS.get(worldStr);
        }
        return 1;// 默认主世界1
    }

    public static int getUserId(String user) {

        USERS = SqliteHelper.getUserMap(); //插入完重置一下worlds
        if (USERS.containsKey(user)) {
            return USERS.get(user);
        }
        return -1;// unknown
        //
    }

    public static void onDeath(DamageSource source, CallbackInfo ci, ServerPlayerEntity serverPlayerEntity) {

      /*  System.out.println("abc1234");
        System.out.println(serverPlayerEntity.getDamageTracker().getEntity());
        System.out.println(source.getAttacker());
        System.out.println(serverPlayerEntity.getDamageTracker().getDeathMessage().getString());
        System.out.println(serverPlayerEntity.getX()+","+serverPlayerEntity.getY()+","+serverPlayerEntity.getZ()+","+ serverPlayerEntity.getWorld().getRegistryKey().getValue().toString());*/
        Entity atta = source.getAttacker();
        String user = serverPlayerEntity.getName().getString();
        SqliteHelper.insertDeath(getUserId(user),getWorldId(serverPlayerEntity.getWorld()),
                (int) Math.floor(serverPlayerEntity.getX()),(int)Math.floor(serverPlayerEntity.getY()), (int)Math.floor(serverPlayerEntity.getZ()),serverPlayerEntity.getDamageTracker().getDeathMessage().getString(),
                atta == null ? "":atta.toString()
        );
    }

    /**
     * 方块破坏
     * @param player
     * @param pos
     * @param blockState
     * @param world
     */
    public static void onBlockBroken(ServerPlayerEntity player, BlockPos pos, BlockState blockState, ServerWorld world) {
        System.out.println("================破坏========================");
        System.out.println("name:" + player.getDisplayName());
        System.out.println("dim:" + world.getRegistryKey().getValue().toString());
        System.out.println("x:" + pos.getX());
        System.out.println("y:" + pos.getY());
        System.out.println("z:" + pos.getZ());
        System.out.println("block:" + blockState.getBlock().getTranslationKey());

        System.out.println(blockState);
    }

    /**
     * 方块放置
     * @param context
     */
    public static void onBlockPlace(ItemPlacementContext context) {
        if (!context.getWorld().isClient) {
            System.out.println("================放置========================");
            BlockState blockState = context.getWorld().getBlockState(context.getBlockPos());
            System.out.println("name:" + context.getPlayer().getDisplayName());
            System.out.println("dim:" + context.getWorld().getRegistryKey().getValue());
            System.out.println("x:" + context.getBlockPos().getX());
            System.out.println("y:" + context.getBlockPos().getY());
            System.out.println("z:" + context.getBlockPos().getZ());
            System.out.println("block:" + blockState.getBlock().getTranslationKey());
            for (Map.Entry<Property<?>, Comparable<?>> propertyComparableEntry : blockState.getEntries().entrySet()) {
                System.out.println(propertyComparableEntry.getKey().getName()+","+propertyComparableEntry.getValue());
            }
            System.out.println(blockState);
            BlockPos blockPos = context.getBlockPos();
          /*  SqliteHelper.insertBlock(getUserId(context.getPlayer().getName().getString()),getWorldId(context.getWorld()),
                    blockPos.getX(),
                    blockPos.getY(),
                    blockPos.getZ(),

                    )*/
        }

    }


    /**
     * 玩家登录
     * @param connection
     * @param player
     */
    public static void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player) {
        String uuid = String.valueOf(player.getUuid());
        String user = player.getName().getString();
        if (!USERS.containsKey(user)) {
            SqliteHelper.insertUser(user,uuid);
            USERS = SqliteHelper.getUserMap();
        }
        SqliteHelper.insertSession(getUserId(user),getWorldId(player.getWorld()),
                (int) Math.floor(player.getX()),(int)Math.floor(player.getY()), (int)Math.floor(player.getZ()),1);
    }

    /**
     * 玩家离线
     * @param ci
     * @param player
     */
    public static void onDisconnect(CallbackInfo ci, ServerPlayerEntity player) {
        String uuid = String.valueOf(player.getUuid());
        String user = player.getName().getString();
        if (!USERS.containsKey(user)) {
            SqliteHelper.insertUser(user,uuid);
            USERS = SqliteHelper.getUserMap();
        }
        SqliteHelper.insertSession(getUserId(user),getWorldId(player.getWorld()),
                (int) Math.floor(player.getX()),(int)Math.floor(player.getY()), (int)Math.floor(player.getZ()),0);
    }
}
