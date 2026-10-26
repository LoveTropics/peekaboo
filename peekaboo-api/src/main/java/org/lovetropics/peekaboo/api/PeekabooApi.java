package org.lovetropics.peekaboo.api;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class PeekabooApi {
    private static @Nullable Impl impl;

    private PeekabooApi() {
    }

    /* package-private */ static Impl impl() {
        return Objects.requireNonNull(impl, "Peekaboo not initialized");
    }

    @ApiStatus.Internal
    public static void registerImpl(Impl impl) {
        if (PeekabooApi.impl != null) {
            throw new IllegalStateException("Implementation already registered");
        }
        PeekabooApi.impl = impl;
    }

    /**
     * Gets the source entity for a given "disguise" entity.
     * Example: if player is disguised as a zombie, this method will return the player entity when given the zombie entity.
     * @param disguseEntity The "disguise" entity.
     * @return The entity the given "disguise" entity is applied for.
    * */
    public static @Nullable Entity getSourceEntity(Entity disguseEntity) {
        return impl().getSourceEntity(disguseEntity);
    }

    public interface Impl {
        @Nullable EntityDisguiseHolder getDisguiseHolder(Entity entity);

        void onDisguiseChange(EntityDisguiseHolder holder, Entity entity);

        @Nullable Entity getSourceEntity(Entity disguseEntity);
    }
}
