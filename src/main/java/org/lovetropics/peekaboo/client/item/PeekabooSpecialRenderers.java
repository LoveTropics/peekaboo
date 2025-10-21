package org.lovetropics.peekaboo.client.item;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import org.lovetropics.peekaboo.PeekabooMod;

@EventBusSubscriber(modid = PeekabooMod.ID, value = Dist.CLIENT)
public class PeekabooSpecialRenderers {
    @SubscribeEvent
    public static void register(RegisterSpecialModelRendererEvent event) {
        event.register(PeekabooMod.location("mob_item"), MobItemSpecialRenderer.Unbaked.MAP_CODEC);
    }
}
