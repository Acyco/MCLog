package cn.acyco.mclog.core;

import cn.acyco.mclog.config.Config;
import cn.acyco.mclog.database.SqliteHelper;
import cn.acyco.mclog.enums.BlockActionType;
import cn.acyco.mclog.ext.AbstractBlockStateExt;
import cn.acyco.mclog.ext.BlockItemExt;
import cn.acyco.mclog.ext.BucketItemBeforeExt;
import cn.acyco.mclog.ext.TrackServerPlayerExt;
import cn.acyco.mclog.model.BlockModel;
import cn.acyco.mclog.utils.TrackBlockUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
            TrackBlock.init();
            config = Config.loadConfig();
            SqliteHelper.createTables();
            init();
        } catch (Exception exception) {
            LOGGER.error("[MCLog] ??????????????????");
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

    public static void serverShutdown() {
        if (SqliteHelper.connection != null) {
            try {
                SqliteHelper.connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            SqliteHelper.connection = null;
            SqliteHelper.databaseFile = null;

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
     * ????????????????????????id
     *
     * @param world ??????
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
     * ??????????????????
     *
     * @param source ???????????????
     * @param player ??????
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

    public static void insertBlock(PlayerEntity player, BlockPos pos, BlockState blockState, World world, BlockActionType actionType) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("time", (int) (new Date().getTime() / 1000));
        map.put("uid", getUserId(player));
        map.put("wid", getWorldId(world));
        map.put("x", pos.getX());
        map.put("y", pos.getY());
        map.put("z", pos.getZ());
        map.put("bid", getBlockId(blockState.getBlock()));
        map.put("sid", getBlockStateId(blockState));
        map.put("action", actionType.getValue());
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



    public static void onBlockBreakBefore(ServerPlayerEntity player, BlockPos pos, BlockState blockState, ServerWorld world, LinkedHashSet<BlockModel> trackBlocks) {
        trackBlocks.clear();
        TrackBlockUtil.trackUp(world, trackBlocks, pos);
        TrackBlockUtil.trackUp(world, trackBlocks, pos.west());
        TrackBlockUtil.trackUp(world, trackBlocks, pos.east());
        TrackBlockUtil.trackUp(world, trackBlocks, pos.down());
        TrackBlockUtil.trackUp(world, trackBlocks, pos.north());
        TrackBlockUtil.trackUp(world, trackBlocks, pos.south());

    }

    public static void onBlockBreakAfter(ServerPlayerEntity player, ServerWorld world, LinkedHashSet<BlockModel> trackBlocks) {
        for (BlockModel trackBlock : trackBlocks) {
            BlockPos blockPos = trackBlock.getBlockPos();
            BlockState oldState = trackBlock.getBlockState();
            BlockState newState= world.getBlockState(blockPos);
            if (!newState.equals(oldState)) {
                if (newState.getBlock().equals(Blocks.AIR)) {
                    insertBlock(player, blockPos, oldState, world, BlockActionType.BREAK);
                } else {
                    //???????????????????????? ????????????????????????
                    insertBlock(player, blockPos, oldState, world, BlockActionType.BREAK);
                    insertBlock(player, blockPos, newState, world, BlockActionType.PLACE);
                }
                DefaultedList<ItemStack> inventory = trackBlock.getInventory();
                if (null != inventory) {
                    for (ItemStack itemStack : inventory) {
                        if (!itemStack.isEmpty()) {
                            insertContainer(blockPos, player, itemStack, 0);// action 0 remove
                        }
                    }
                }
            }
        }

    }



    /**
     * ??????????????????
     *
     * @param context   ?????????
     * @param blockItem ???????????????????????????
     */
    public static void onBlockPlace(ItemPlacementContext context, BlockItem blockItem) {
        if (!context.getWorld().isClient) {
            BlockPos blockPos = context.getBlockPos();
            BlockState blockState = context.getWorld().getBlockState(blockPos);
            PlayerEntity player = context.getPlayer();
            BlockItemExt blockItemExt = (BlockItemExt) blockItem;
            BlockState beforeState = blockItemExt.getBeforeState();
            World world = context.getWorld();
            insertBlock(player, blockPos, blockState, world, BlockActionType.PLACE);
            doBlockSidePlace(beforeState,blockState,player,blockPos,blockItemExt,world);
        }
    }


    private static void doBlockSidePlace(BlockState beforeState, BlockState blockState, PlayerEntity player, BlockPos blockPos, BlockItemExt blockItemExt, World world) {
        if (beforeState != null) {
            if (!beforeState.getFluidState().isEmpty() && (blockState.contains(Properties.WATERLOGGED) && !blockState.get(Properties.WATERLOGGED))) { // ???????????????????????????(????????????)???????????????????????????!isWaterlogged(blockState)

       /*         onBlockBreakBefore((ServerPlayerEntity) player, blockPos, blockItemExt.getBeforeFluidState().getBlockState(), (ServerWorld) world, trackBlocks);*/
                blockItemExt.setBeforeState(null); //??????????????????null
            }
        }
        Block block = blockState.getBlock();
        //bed
        if (TrackBlock.contains(TrackBlock.BED, block)) {
            BedPart bedPart = blockState.get(BedBlock.PART);
            Direction blockDirection = blockState.get(BedBlock.FACING);
            Direction direction = bedPart == BedPart.FOOT ? blockDirection : blockDirection.getOpposite();
            BlockPos anotherPos = blockPos.offset(direction);
            insertBlock(player, anotherPos, world.getBlockState(anotherPos), world, BlockActionType.PLACE);

        }
    }


    /**
     * ????????????
     *
     * @param player ??????
     */
    public static void onPlayerConnect(ServerPlayerEntity player) {
        insertSession(player, 1);
    }

    /**
     * ????????????
     *
     * @param player ??????
     */
    public static void onDisconnect(ServerPlayerEntity player) {
        insertSession(player, 0);
    }


    public static void inventoryUpdate(PlayerEntity player, ItemStack beforeItemStack, ItemStack afterItemStack, BlockPos blockPos) {
        if (beforeItemStack == null || afterItemStack == null) return;
        if (beforeItemStack.isEmpty() && afterItemStack.isEmpty()) return;


        if (!beforeItemStack.isEmpty() && !afterItemStack.isEmpty()) { //????????????????????????
            if (afterItemStack.getItem() == beforeItemStack.getItem()) { // ???????????????????????????

                int beforeCount = beforeItemStack.getCount();
                int afterCount = afterItemStack.getCount();

                if (afterCount < beforeCount) { //????????????????????????????????? remove
                    inventoryUpdate(player, new ItemStack(afterItemStack.getItem(), beforeCount - afterCount), ItemStack.EMPTY, blockPos);

                } else {
                    //???????????????????????????
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


    public static void onBucketUse(World world, PlayerEntity user, BucketItemBeforeExt bucketItemBeforeExt, BlockActionType actionType) {
        if (world.isClient) {
            return;
        }
        BlockState blockState = bucketItemBeforeExt.getBlockState();
        BlockPos blockPos = bucketItemBeforeExt.getBlockPos();
        if (actionType == BlockActionType.PLACE) {
            //?????????????????? ?????????????????????????????????????????????
            blockState = world.getFluidState(blockPos).getBlockState();
        }
        insertBlock(user, blockPos, blockState, world, actionType);
    }


    public static void onInteractBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {

        if (cir.getReturnValue().isAccepted()) {
            //

            BlockPos blockPos = hitResult.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);

            /*        player.getCommandSource().withLookingAt(hitResult.getBlockPos().offset(hitResult.getSide()));*/
            //BlockPos blockPos1=  blockPos.offset(hitResult.getSide());
            //System.out.println(blockPos);
            //System.out.println(blockPos1);
            //System.out.println(blockState);
            //System.out.println(world.getBlockState(blockPos1));
        }
    }

    public static void onUse(World world, PlayerEntity player, Hand hand, BlockHitResult hit, AbstractBlock.AbstractBlockState abstractBlockState) {
        if (abstractBlockState instanceof AbstractBlockStateExt abstractBlockStateExt) {
            BlockPos blockPos = hit.getBlockPos();
            if (!(abstractBlockState.getBlock() instanceof ChestBlock))
                insertBlock(player, blockPos, abstractBlockStateExt.getBeforeBlockState(), world, BlockActionType.CLICK);
        }
    }

    public static void placeFluid(BucketItem bucketItem, PlayerEntity player, World world, BlockPos pos, BlockHitResult hitResult, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient) {
            System.out.println(cir.getReturnValueZ());
            System.out.println(player);
            System.out.println(world);
            System.out.println(pos);

            System.out.println(hitResult);
        }

    }

    // items use begin
    public static void flintAndSteelItemUserOnBlock(Args args, ItemUsageContext context) {

        World world = context.getWorld();
        if (world.isClient) {
            return;
        }
        BlockPos pos = args.get(0);
        BlockState AfterState = args.get(1);
        BlockState beforeState = world.getBlockState(pos);

        PlayerEntity playerEntity = context.getPlayer();
        if (playerEntity instanceof ServerPlayerEntity serverPlayer) {

            TrackServerPlayerExt blockStateExt = (TrackServerPlayerExt) AfterState;
            blockStateExt.setMclogTrackPlayer(serverPlayer);//?????????????????????????????????????????????
        }


        if (AfterState.getBlock().equals(beforeState.getBlock())) {
            //?????????????????????????????????
            insertBlock(playerEntity, pos, beforeState, world, BlockActionType.BREAK); //?????????
        }
        insertBlock(playerEntity, pos, AfterState, world, BlockActionType.PLACE); //?????????

    }

    public static void shovelOrAxeOrHoneycombItemUserOnBlock(Args args, ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) {
            return;
        }
        BlockState AfterState = args.get(1);
        BlockPos pos = args.get(0);
        BlockState beforeState = world.getBlockState(pos);
        System.out.println(beforeState);
        System.out.println(AfterState);
        insertBlock(context.getPlayer(), pos, beforeState, world, BlockActionType.BREAK); //?????????
        insertBlock(context.getPlayer(), pos, AfterState, world, BlockActionType.PLACE); //?????????
    }

    public static void hoeItemUserOnBlock(BlockState AfterState, ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) {
            return;
        }
        BlockPos pos = context.getBlockPos();
        BlockState beforeState = world.getBlockState(pos);
        insertBlock(context.getPlayer(), pos, beforeState, world, BlockActionType.BREAK); //?????????
        insertBlock(context.getPlayer(), pos, AfterState, world, BlockActionType.PLACE); //?????????
    }
    // items use end

    public static void onAffectWorld(@Nullable Entity entity, World world, BlockPos blockPos, BlockState blockState) {
        if (world.isClient) return;

        if (entity instanceof CreeperEntity creeper) {
            LivingEntity livingEntity = creeper.getTarget();
            if (livingEntity instanceof PlayerEntity player) {
                insertBlock(player, blockPos, blockState, world, BlockActionType.EXPLODE);
            }
        }
        if (blockState.getBlock().getDefaultState().getBlock().equals(Blocks.TNT)) return; //tnt????????????e
        if (entity instanceof TrackServerPlayerExt trackServerPlayerExt && !blockState.getBlock().equals(Blocks.FIRE)) {
            //todo ???????????????????????????????????????????????????????????????
            ServerPlayerEntity serverPlayer = trackServerPlayerExt.getMclogTrackPlayer();
            if (serverPlayer != null) {
                insertBlock(serverPlayer, blockPos, blockState, world, BlockActionType.EXPLODE);
            }
        }
    }

    public static void fireBlockTryRemoveBlockOrTrySetBlockState(World world, BlockPos pos, ServerPlayerEntity player) {
        igniteBlock(world, pos, world.getBlockState(pos), player);

    }

    public static void igniteTnt(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer)
            igniteBlock(world, pos, state, serverPlayer);
    }

    private static void igniteBlock(World world, BlockPos pos, BlockState blockState, ServerPlayerEntity player) {
        if (world.isClient) {
            return;
        }
        insertBlock(player, pos, world.getBlockState(pos), world, BlockActionType.IGNITE); //??????
    }



}
