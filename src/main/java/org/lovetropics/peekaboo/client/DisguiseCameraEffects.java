package org.lovetropics.peekaboo.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import org.lovetropics.peekaboo.PeekabooMod;
import org.lovetropics.peekaboo.diguise.EntityDisguiseHolder;

@EventBusSubscriber(modid = PeekabooMod.ID, value = Dist.CLIENT)
public class DisguiseCameraEffects {
    @SubscribeEvent
    public static void calculateCameraDistance(CalculateDetachedCameraDistanceEvent event) {
        EntityDisguiseHolder disguise = EntityDisguiseHolder.getOrNull(event.getCamera().getEntity());
        if (disguise == null) {
            return;
        }
        float scale = Math.max(disguise.getEffectiveScale(), 1.0f);
        event.setDistance(event.getDistance() * scale);
    }
}
