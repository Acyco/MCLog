package cn.acyco.mclog;

import cn.acyco.mclog.config.Config;
import cn.acyco.mclog.database.SqliteHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

/**
 * @author Acyco
 * @create 2022-01-01 13:03
 * @url https://acyco.cn
 */
public class MCLogCore {
    public static MinecraftServer server;
    public static Config config;
    public static final Logger LOGGER = LogManager.getLogger("MCLog");
    public static File savePath;

    public static void serverLoaded(MinecraftServer minecraftServer) {
        System.out.println("mc server loaded");
        try {
            server = minecraftServer;
            config = Config.loadConfig();
            SqliteHelper.createTables();

        } catch (Exception exception) {
            LOGGER.error("[MCLog] 配置加载失败");
        }
    }

    public static File getPathFile(String name) {
        return server.getSavePath(WorldSavePath.ROOT).resolve(name).toFile();
    }

    public static void onDeath(DamageSource source, CallbackInfo ci, ServerPlayerEntity serverPlayerEntity) {

        System.out.println("abc1234");
        System.out.println(serverPlayerEntity.getDamageTracker().getEntity());
        System.out.println(source.getAttacker());
        System.out.println(serverPlayerEntity.getDamageTracker().getDeathMessage().getString());

        System.out.println(serverPlayerEntity.getX()+","+serverPlayerEntity.getY()+","+serverPlayerEntity.getZ()+","+ serverPlayerEntity.getWorld().getRegistryKey().getValue().toString());
    }

    public static void onBlockBroken(ServerPlayerEntity player, BlockPos pos, BlockState blockState, ServerWorld world) {
        System.out.println("name:" + player.getDisplayName());
        System.out.println("dim:" + world.getRegistryKey().getValue().toString());
        System.out.println("x:" + pos.getX());
        System.out.println("y:" + pos.getY());
        System.out.println("z:" + pos.getZ());
        System.out.println("block:" + blockState.getBlock().getTranslationKey());
    }

    public static void onBlockPlace(ItemPlacementContext context) {
        if (!context.getWorld().isClient) {
            BlockState blockState = context.getWorld().getBlockState(context.getBlockPos());
            System.out.println("name:" + context.getPlayer().getDisplayName());
            System.out.println("dim:" + context.getWorld().getRegistryKey().getValue());
            System.out.println("x:" + context.getBlockPos().getX());
            System.out.println("y:" + context.getBlockPos().getY());
            System.out.println("z:" + context.getBlockPos().getZ());
            System.out.println("block:" + blockState.getBlock().getTranslationKey());
        }

    }


    public static void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player) {
        String world = player.getWorld().getRegistryKey().getValue().toString();
        /*
        String uuid = String.valueOf(player.getUuid());
        System.out.println("uuid:"+ uuid);
        System.out.println("name:" + player.getName().getString());
        System.out.println("dim:" + player.getWorld().getRegistryKey().getValue());
        System.out.println("x:" + player.getX());
        System.out.println("y:" + player.getY());
        System.out.println("z:" + player.getZ());
*/



    }
}
