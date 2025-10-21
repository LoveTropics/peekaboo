package org.lovetropics.peekaboo.diguise;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.lovetropics.peekaboo.PeekabooMod;
import org.lovetropics.peekaboo.network.DisguiseSynchronizer;

import javax.annotation.Nullable;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class EntityDisguiseHolder {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PeekabooMod.ID);

    public static final Supplier<AttachmentType<EntityDisguiseHolder>> ATTACHMENT = ATTACHMENT_TYPES.register(
            "disguise", () -> AttachmentType.builder(holder -> new EntityDisguiseHolder((LivingEntity) holder))
                    .serialize(new IAttachmentSerializer<>() {
                        @Override
                        public EntityDisguiseHolder read(IAttachmentHolder holder, ValueInput input) {
                            EntityDisguiseHolder disguise = new EntityDisguiseHolder((LivingEntity) holder);
                            disguise.set(input.read("disguise", Disguise.CODEC).orElse(Disguise.NONE));
                            return disguise;
                        }

                        @Override
                        public boolean write(EntityDisguiseHolder attachment, ValueOutput output) {
                            output.store("disguise", Disguise.CODEC, attachment.disguise);
                            return true;
                        }
                    }).build()
    );

    private final LivingEntity entity;

    private Disguise disguise = Disguise.NONE;
    @Nullable
    private Entity disguisedEntity;

    private EntityDisguiseHolder(LivingEntity entity) {
        this.entity = entity;
    }

    @Nullable
    public static EntityDisguiseHolder getOrNull(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity.getData(ATTACHMENT);
        }
        return null;
    }

    public static Disguise getDisguise(Entity entity) {
        EntityDisguiseHolder holder = getOrNull(entity);
        return holder != null ? holder.disguise() : Disguise.NONE;
    }

    public static void set(LivingEntity entity, Disguise disguise) {
        EntityDisguiseHolder holder = getOrNull(entity);
        if (holder != null) {
            holder.set(disguise);
            DisguiseSynchronizer.broadcastDisguise(entity);
        }
    }

    public static Disguise update(LivingEntity entity, UnaryOperator<Disguise> operator) {
        EntityDisguiseHolder holder = getOrNull(entity);
        if (holder != null) {
            Disguise newDisguise = operator.apply(holder.disguise());
            holder.set(newDisguise);
            DisguiseSynchronizer.broadcastDisguise(entity);
            return newDisguise;
        }
        return Disguise.NONE;
    }

    public void clear() {
        set(Disguise.NONE);
    }

    public void clear(Disguise disguise) {
        set(this.disguise.clear(disguise));
    }

    public void set(Disguise disguise) {
        if (this.disguise.equals(disguise)) {
            return;
        }
        this.disguise = disguise;
        disguisedEntity = disguise.createEntity(entity.level());
        onDisguiseChange();
    }

    private void onDisguiseChange() {
        DisguiseBehavior.onDisguiseChange(entity);
        if (!entity.level().isClientSide()) {
            DisguiseSynchronizer.broadcastDisguise(entity);
        }
    }

    public Disguise disguise() {
        return disguise;
    }

    @Nullable
    public Entity entity() {
        return disguisedEntity;
    }

    public void copyFrom(EntityDisguiseHolder from) {
        set(from.disguise());
    }

    public float getEffectiveScale() {
        if (disguisedEntity != null) {
            float entityScale = disguisedEntity.getBbHeight() / EntityType.PLAYER.getHeight();
            return disguise.scale() * entityScale;
        } else {
            return disguise.scale();
        }
    }
}
