package org.lovetropics.peekaboo.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.lovetropics.peekaboo.PeekabooMod;
import org.lovetropics.peekaboo.api.Disguise;

public record ClientboundSetDisguisePacket(int entityId, Disguise disguise) implements CustomPacketPayload {
    public static final Type<ClientboundSetDisguisePacket> TYPE = new Type<>(PeekabooMod.location("set_disguise"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSetDisguisePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ClientboundSetDisguisePacket::entityId,
            Disguise.STREAM_CODEC, ClientboundSetDisguisePacket::disguise,
            ClientboundSetDisguisePacket::new
    );

    @Override
    public Type<ClientboundSetDisguisePacket> type() {
        return TYPE;
    }
}
