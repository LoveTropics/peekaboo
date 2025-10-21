package org.lovetropics.peekaboo.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import org.lovetropics.peekaboo.PeekabooMod;
import org.lovetropics.peekaboo.api.EntityDisguiseHolder;

@EventBusSubscriber(modid = PeekabooMod.ID, value = Dist.CLIENT)
public class DisguiseCameraEffects {
    @SubscribeEvent
    public static void calculateCameraDistance(CalculateDetachedCameraDistanceEvent event) {
        EntityDisguiseHolder holder = EntityDisguiseHolder.getOrNull(event.getCamera().getEntity());
        if (holder == null) {
            return;
        }
        float scale = Math.max(getEffectiveScale(holder), 1.0f);
        event.setDistance(event.getDistance() * scale);
    }

    private static float getEffectiveScale(EntityDisguiseHolder holder) {
        Entity entity = holder.entity();
        if (entity != null) {
            float entityScale = entity.getBbHeight() / EntityType.PLAYER.getHeight();
            return holder.disguise().scale() * entityScale;
        } else {
            return holder.disguise().scale();
        }
    }
}
