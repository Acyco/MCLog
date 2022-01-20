package cn.acyco.mclog.ext;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Acyco
 * @create 2022-01-20 00:47
 * @url https://acyco.cn
 */
public interface TrackServerPlayerExt {
    public ServerPlayerEntity getMclogTrackPlayer();
    public void setMclogTrackPlayer(ServerPlayerEntity serverPlayer);
}
