package org.lovetropics.peekaboo.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
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
        boolean hideShadow,
        boolean useDisguiseArmor
) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ContextKey<DisguiseRenderState> KEY = new ContextKey<>(PeekabooMod.identifier("disguise"));

    private static DisguiseRenderState extractWithoutEntity(Disguise disguise) {
        return new DisguiseRenderState(null, disguise.scale(), disguise.hideShadow(), disguise.useDisuseEquipment());
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
            copyDisguiseState(disguiseEntity, entity, disguiseHolder.disguise().useDisuseEquipment());
            disguiseEntity.setCustomNameVisible(renderState.nameTag != null);

            EntityRenderState disguiseEntityState = renderer.createRenderState(disguiseEntity, renderState.partialTick);
            if (disguiseHolder.disguise().hideShadow()) {
                disguiseEntityState.shadowPieces.clear();
            }

            return new DisguiseRenderState(
                    disguiseEntityState,
                    disguiseHolder.disguise().scale(),
                    disguiseHolder.disguise().hideShadow(),
                    disguiseHolder.disguise().useDisuseEquipment()
            );
        } catch (Exception e) {
            disguiseHolder.clear();
            LOGGER.error("Failed to capture disguise state", e);
        }

        return extractWithoutEntity(disguiseHolder.disguise());
    }

    private static void copyDisguiseState(Entity disguise, LivingEntity entity, boolean useDisguiseArmor) {
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
            copyLivingDisguiseState(livingDisguise, entity, useDisguiseArmor);
        }

        disguise.tickCount = entity.tickCount;
    }

    private static void copyLivingDisguiseState(LivingEntity disguise, LivingEntity entity, boolean useDisguiseEquipment) {
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

        if (!useDisguiseEquipment) {
            for (EquipmentSlot slot : EquipmentSlot.VALUES) {
                ItemStack stack = entity.getItemBySlot(slot);
                if (!stack.is(PeekabooItems.DISGUISE)) {
                    disguise.setItemSlot(slot, stack);
                }
            }
        }

        copyAttachments(disguise, entity);
    }

    private static void copyAttachments(LivingEntity disguise, LivingEntity entity) {
        for (Holder<AttachmentType<?>> attachmentTypeHolder : NeoForgeRegistries.ATTACHMENT_TYPES.asHolderIdMap()) {
            if (attachmentTypeHolder.is(PeekabooMod.COPY_RENDER_ATTACHMENTS)) {
                AttachmentType<?> attachmentType = attachmentTypeHolder.value();
                copyAttachment(disguise, entity, attachmentType);
            }
        }
    }

    private static <T> void copyAttachment(LivingEntity disguise, LivingEntity entity, AttachmentType<T> attachmentType) {
        T attachment = entity.getExistingDataOrNull(attachmentType);
        if (attachment == null) {
            return;
        }
        disguise.setData(attachmentType, attachment);
    }
}
