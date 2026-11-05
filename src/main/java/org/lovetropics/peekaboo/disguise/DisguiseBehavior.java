package org.lovetropics.peekaboo.disguise;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;
import org.lovetropics.peekaboo.PeekabooMod;
import org.lovetropics.peekaboo.api.Disguise;
import org.lovetropics.peekaboo.api.EntityDisguiseHolder;
import org.lovetropics.peekaboo.duck.ExtendedWalkAnimationState;

import java.util.Objects;

@EventBusSubscriber(modid = PeekabooMod.ID)
public final class DisguiseBehavior {
    private static final float EYE_BOUNDS_BUFFER = 1.0f / 16.0f;

    @SubscribeEvent
    public static void onSetEntitySize(EntityEvent.Size event) {
        EntityDisguiseHolder disguiseHolder = EntityDisguiseHolder.getOrNull(event.getEntity());
        if (disguiseHolder == null || disguiseHolder.disguise().isEmpty()) {
            return;
        }

        Entity entity = Objects.requireNonNullElse(disguiseHolder.entity(), event.getEntity());
        float scale = disguiseHolder.disguise().scale();

        Pose pose = event.getPose();
        EntityDimensions disguiseDimensions = entity.getDimensions(pose);
        EntityDimensions actualDimensions;
        if (disguiseHolder.disguise().changesSize()) {
            actualDimensions = disguiseDimensions;
        } else {
            EntityDimensions originalDimensions = event.getEntity().getDimensions(pose);
            actualDimensions = new EntityDimensions(
                    originalDimensions.width(),
                    originalDimensions.height(),
                    // Don't let the eye height leave the player bounds, it just causes clipping
                    Mth.clamp(disguiseDimensions.eyeHeight(), EYE_BOUNDS_BUFFER, originalDimensions.height() - EYE_BOUNDS_BUFFER),
                    disguiseDimensions.attachments(),
                    originalDimensions.fixed()
            );
        }

        event.setNewSize(actualDimensions.scale(scale));
    }

    public static void copyWalkAnimation(WalkAnimationState from, WalkAnimationState to) {
        ((ExtendedWalkAnimationState) to).peekaboo$copyFrom(from);
    }

    public static void onDisguiseChange(LivingEntity entity) {
        entity.refreshDimensions();
        if (entity instanceof Player player) {
            player.refreshDisplayName();
        }
        // Can be triggered from deserialization before the player connection is set up - no point to send the packet then anyway
        if (entity instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
            serverPlayer.refreshTabListName();
        }
    }

    @SubscribeEvent
    public static void onPlayerNameFormat(PlayerEvent.NameFormat event) {
        event.setDisplayname(updateDisplayName(event.getEntity(), event.getDisplayname()));
    }

    @SubscribeEvent
    public static void onPlayerTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        event.setDisplayName(updateDisplayName(event.getEntity(), event.getDisplayName()));
    }

    @Contract("_,!null->!null")
    private static @Nullable Component updateDisplayName(Player player, @Nullable Component name) {
        Disguise disguise = EntityDisguiseHolder.getDisguise(player);
        return disguise.customName().orElse(name);
    }
}
