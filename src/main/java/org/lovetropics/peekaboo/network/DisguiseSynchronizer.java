package org.lovetropics.peekaboo.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lovetropics.peekaboo.PeekabooMod;
import org.lovetropics.peekaboo.api.EntityDisguiseHolder;

@EventBusSubscriber(modid = PeekabooMod.ID)
public class DisguiseSynchronizer {
    @SubscribeEvent
    public static void onEntityTrack(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendInitialDisguise(player, event.getTarget());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendInitialDisguise(player, player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendInitialDisguise(player, player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newPlayer) {
            sendInitialDisguise(newPlayer, newPlayer);
        }
    }

    private static void sendInitialDisguise(ServerPlayer player, Entity tracked) {
        EntityDisguiseHolder disguiseHolder = EntityDisguiseHolder.getOrNull(tracked);
        if (disguiseHolder != null && !disguiseHolder.disguise().isEmpty()) {
            PacketDistributor.sendToPlayer(
                    player,
                    new ClientboundSetDisguisePacket(tracked.getId(), disguiseHolder.disguise())
            );
        }
    }

    public static void broadcastDisguise(LivingEntity entity) {
        EntityDisguiseHolder disguiseHolder = EntityDisguiseHolder.getOrNull(entity);
        if (disguiseHolder != null) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new ClientboundSetDisguisePacket(entity.getId(), disguiseHolder.disguise()));
        }
    }
}
