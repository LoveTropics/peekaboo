package org.lovetropics.peekaboo.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.lovetropics.peekaboo.PeekabooMod;

@EventBusSubscriber(modid = PeekabooMod.ID)
public class PeekabooNetwork {
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(ClientboundSetDisguisePacket.TYPE, ClientboundSetDisguisePacket.STREAM_CODEC);
    }
}
