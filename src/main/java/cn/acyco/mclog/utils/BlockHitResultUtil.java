package cn.acyco.mclog.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

/**
 * @author Acyco
 * @create 2022-01-12 01:20
 * @url https://acyco.cn
 */
public class BlockHitResultUtil {

    public static BlockHitResult raycast(World world, PlayerEntity player, RaycastContext.FluidHandling fluidHandling) {
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

}
