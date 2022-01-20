package cn.acyco.mclog.mixin;

import cn.acyco.mclog.ext.TrackServerPlayerExt;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author Acyco
 * @create 2022-01-20 01:17
 * @url https://acyco.cn
 */
@Mixin(Entity.class)
public abstract class EntityMixin implements TrackServerPlayerExt {
    private ServerPlayerEntity mclogTrackPlayer;


    @Override
    public ServerPlayerEntity getMclogTrackPlayer() {
        return this.mclogTrackPlayer;
    }

    @Override
    public void setMclogTrackPlayer(ServerPlayerEntity mclogTrackPlayer) {
        this.mclogTrackPlayer = mclogTrackPlayer;
    }
}
