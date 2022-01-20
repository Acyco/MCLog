package cn.acyco.mclog.mixin;

import cn.acyco.mclog.ext.TrackServerPlayerExt;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author Acyco
 * @create 2022-01-20 00:47
 * @url https://acyco.cn
 */
@Mixin(BlockState.class)
public abstract class BlockStateMixin implements TrackServerPlayerExt {

    private ServerPlayerEntity mclogTrackPlayer;

    @Override
    public ServerPlayerEntity getMclogTrackPlayer() {
        return this.mclogTrackPlayer;
        //攻击
    }

    @Override
    public void setMclogTrackPlayer(ServerPlayerEntity serverPlayer) {
        this.mclogTrackPlayer = serverPlayer;
    }
}
