package org.lovetropics.peekaboo.api;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Optional;

// Can be replaced in 1.21.9+ with TypedEntityData
public record TypedEntityData(CustomData data) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Codec<TypedEntityData> CODEC = CustomData.CODEC.comapFlatMap(
            data -> {
                ResourceLocation entityId = data.parseEntityId();
                Optional<EntityType<?>> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId);
                if (entityType.isEmpty()) {
                    return DataResult.error(() -> "No entity with id: '" + entityId + "'");
                }
                return DataResult.success(new TypedEntityData(data));
            },
            TypedEntityData::data
    );

    public static final StreamCodec<ByteBuf, TypedEntityData> STREAM_CODEC = CustomData.STREAM_CODEC
            .map(TypedEntityData::new, TypedEntityData::data);

    public TypedEntityData(EntityType<?> type) {
        this(type, new CompoundTag());
    }

    public TypedEntityData(EntityType<?> type, CompoundTag tag) {
        this(CustomData.of(tag).update(t -> t.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString())));
    }

    public EntityType<?> type() {
        return BuiltInRegistries.ENTITY_TYPE.getValue(data.parseEntityId());
    }

    @Nullable
    public Entity createEntity(Level level) {
        try {
            Entity entity = type().create(level, EntitySpawnReason.LOAD);
            if (entity == null) {
                return null;
            }
            data.loadInto(entity);
            fixInvalidEntity(entity);
            return entity;
        } catch (Exception e) {
            LOGGER.error("Failed to create entity for disguise: {}", this, e);
            return null;
        }
    }

    private void fixInvalidEntity(Entity entity) {
        entity.stopRiding();
    }
}
