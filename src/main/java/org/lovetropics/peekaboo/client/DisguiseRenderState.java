package org.lovetropics.peekaboo.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.lovetropics.peekaboo.PeekabooMod;
import org.lovetropics.peekaboo.api.Disguise;
import org.lovetropics.peekaboo.api.EntityDisguiseHolder;
import org.lovetropics.peekaboo.disguise.DisguiseBehavior;
import org.lovetropics.peekaboo.item.PeekabooItems;
import org.slf4j.Logger;

public record DisguiseRenderState(
        @Nullable EntityRenderState entityRenderState,
        float scale,
        boolean hideShadow
) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ContextKey<DisguiseRenderState> KEY = new ContextKey<>(PeekabooMod.identifier("disguise"));

    private static DisguiseRenderState extractWithoutEntity(Disguise disguise) {
        return new DisguiseRenderState(null, disguise.scale(), disguise.hideShadow());
    }

    public static @Nullable DisguiseRenderState extract(LivingEntity entity, LivingEntityRenderState renderState) {
        EntityDisguiseHolder disguiseHolder = EntityDisguiseHolder.getOrNull(entity);
        if (disguiseHolder == null || disguiseHolder.disguise().isEmpty()) {
            return null;
        }
        Entity disguiseEntity = disguiseHolder.entity();
        if (disguiseEntity == null) {
            return extractWithoutEntity(disguiseHolder.disguise());
        }
        return extract(entity, renderState, disguiseEntity, disguiseHolder);
    }

    private static <E extends Entity> DisguiseRenderState extract(LivingEntity entity, LivingEntityRenderState renderState, E disguiseEntity, EntityDisguiseHolder disguiseHolder) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super E, ?> renderer = entityRenderDispatcher.getRenderer(disguiseEntity);
        if (renderer == null) {
            return extractWithoutEntity(disguiseHolder.disguise());
        }

        try {
            copyDisguiseState(disguiseEntity, entity);
            disguiseEntity.setCustomNameVisible(renderState.nameTag != null);

            return new DisguiseRenderState(
                    renderer.createRenderState(disguiseEntity, renderState.partialTick),
                    disguiseHolder.disguise().scale(),
                    disguiseHolder.disguise().hideShadow()
            );
        } catch (Exception e) {
            disguiseHolder.clear();
            LOGGER.error("Failed to capture disguise state", e);
        }

        return extractWithoutEntity(disguiseHolder.disguise());
    }

    private static void copyDisguiseState(Entity disguise, LivingEntity entity) {
        disguise.setPos(entity.getX(), entity.getY(), entity.getZ());
        disguise.xo = entity.xo;
        disguise.yo = entity.yo;
        disguise.zo = entity.zo;
        disguise.xOld = entity.xOld;
        disguise.yOld = entity.yOld;
        disguise.zOld = entity.zOld;

        disguise.setYRot(entity.getYRot());
        disguise.yRotO = entity.yRotO;
        disguise.setXRot(entity.getXRot());
        disguise.xRotO = entity.xRotO;

        disguise.setShiftKeyDown(entity.isShiftKeyDown());
        disguise.setPose(entity.getPose());
        disguise.setInvisible(entity.isInvisible());
        disguise.setSprinting(entity.isSprinting());
        disguise.setSwimming(entity.isSwimming());

        disguise.setCustomName(entity.getDisplayName());
        disguise.setCustomNameVisible(entity.isCustomNameVisible());
        disguise.setGlowingTag(entity.isCurrentlyGlowing());

        if (disguise instanceof LivingEntity livingDisguise) {
            copyLivingDisguiseState(livingDisguise, entity);
        }

        disguise.tickCount = entity.tickCount;
    }

    private static void copyLivingDisguiseState(LivingEntity disguise, LivingEntity entity) {
        disguise.yBodyRot = entity.yBodyRot;
        disguise.yBodyRotO = entity.yBodyRotO;

        disguise.yHeadRot = entity.yHeadRot;
        disguise.yHeadRotO = entity.yHeadRotO;

        DisguiseBehavior.copyWalkAnimation(entity.walkAnimation, disguise.walkAnimation);

        disguise.swingingArm = entity.swingingArm;
        disguise.attackAnim = entity.attackAnim;
        disguise.swingTime = entity.swingTime;
        disguise.oAttackAnim = entity.oAttackAnim;
        disguise.swinging = entity.swinging;

        disguise.setOnGround(entity.onGround());

        disguise.hurtTime = entity.hurtTime;
        disguise.hurtDuration = entity.hurtDuration;
        disguise.hurtMarked = entity.hurtMarked;

        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.is(PeekabooItems.DISGUISE)) {
                disguise.setItemSlot(slot, stack);
            }
        }
    }
}
