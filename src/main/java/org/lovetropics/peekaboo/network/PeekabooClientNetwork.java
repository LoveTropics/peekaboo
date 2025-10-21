package org.lovetropics.peekaboo.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.lovetropics.peekaboo.PeekabooMod;
import org.lovetropics.peekaboo.api.EntityDisguiseHolder;

@EventBusSubscriber(modid = PeekabooMod.ID, value = Dist.CLIENT)
public class PeekabooClientNetwork {
    @SubscribeEvent
    public static void registerClientHandler(RegisterClientPayloadHandlersEvent event) {
        event.register(ClientboundSetDisguisePacket.TYPE, PeekabooClientNetwork::handleDisguise);
    }

    private static void handleDisguise(ClientboundSetDisguisePacket packet, IPayloadContext context) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.getEntity(packet.entityId()) instanceof LivingEntity entity) {
            EntityDisguiseHolder disguiseHolder = EntityDisguiseHolder.getOrNull(entity);
            if (disguiseHolder != null) {
                disguiseHolder.set(packet.disguise());
            }
        }
    }
}
