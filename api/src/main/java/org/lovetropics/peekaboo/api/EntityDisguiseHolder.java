package org.lovetropics.peekaboo.api;

import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.function.UnaryOperator;

public class EntityDisguiseHolder {
    private final Entity entity;

    private Disguise disguise = Disguise.NONE;
    @Nullable
    private Entity disguisedEntity;

    public EntityDisguiseHolder(Entity entity) {
        this.entity = entity;
    }

    @Nullable
    public static EntityDisguiseHolder getOrNull(Entity entity) {
        return PeekabooApi.impl().getDisguiseHolder(entity);
    }

    public static Disguise getDisguise(Entity entity) {
        EntityDisguiseHolder holder = getOrNull(entity);
        return holder != null ? holder.disguise() : Disguise.NONE;
    }

    public static void set(Entity entity, Disguise disguise) {
        EntityDisguiseHolder holder = getOrNull(entity);
        if (holder != null) {
            holder.set(disguise);
        }
    }

    public static Disguise update(Entity entity, UnaryOperator<Disguise> operator) {
        EntityDisguiseHolder holder = getOrNull(entity);
        if (holder != null) {
            Disguise newDisguise = operator.apply(holder.disguise());
            holder.set(newDisguise);
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
        PeekabooApi.impl().onDisguiseChange(this, entity);
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
}
