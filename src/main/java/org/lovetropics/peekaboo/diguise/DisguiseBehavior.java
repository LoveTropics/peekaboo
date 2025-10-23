package org.lovetropics.peekaboo.diguise;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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
import org.lovetropics.peekaboo.PeekabooMod;
import org.lovetropics.peekaboo.api.Disguise;
import org.lovetropics.peekaboo.api.EntityDisguiseHolder;

import javax.annotation.Nullable;
import java.util.Objects;

@EventBusSubscriber(modid = PeekabooMod.ID)
public final class DisguiseBehavior {
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
            actualDimensions = event.getEntity().getDimensions(pose).withEyeHeight(disguiseDimensions.eyeHeight());
        }

        event.setNewSize(actualDimensions.scale(scale));
    }

    public static void copyWalkAnimation(WalkAnimationState from, WalkAnimationState to) {
        to.update(from.position() - to.position() - from.speed(), 1.0f, 1.0f);
        to.setSpeed(from.speed(0.0f));
        to.update(from.speed(), 1.0f, 1.0f);
    }

    public static void onDisguiseChange(LivingEntity entity) {
        entity.refreshDimensions();
        if (entity instanceof Player player) {
            player.refreshDisplayName();
        }
        if (entity instanceof ServerPlayer serverPlayer) {
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
    @Nullable
    private static Component updateDisplayName(Player player, @Nullable Component name) {
        Disguise disguise = EntityDisguiseHolder.getDisguise(player);
        return disguise.customName().orElse(name);
    }
}
