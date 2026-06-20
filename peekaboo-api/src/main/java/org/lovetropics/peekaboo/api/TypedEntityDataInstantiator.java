package org.lovetropics.peekaboo.api;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class TypedEntityDataInstantiator {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static @Nullable Entity instantiate(TypedEntityData<EntityType<?>> data, Level level) {
        try {
            Entity entity = data.type().create(level, EntitySpawnReason.LOAD);
            if (entity == null) {
                return null;
            }
            entity.setId(1);
            // The entity constructor randomizes entity rotation for some reason
            entity.setYRot(0.0f);
            entity.setYHeadRot(0.0f);
            entity.setYBodyRot(0.0f);
            data.loadInto(entity);
            fixInvalidEntity(entity);
            return entity;
        } catch (Exception e) {
            LOGGER.error("Failed to create entity for disguise: {}", data, e);
            return null;
        }
    }

    private static void fixInvalidEntity(Entity entity) {
        entity.stopRiding();
    }
}
