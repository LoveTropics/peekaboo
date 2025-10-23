package org.lovetropics.peekaboo.api;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Objects;

public class PeekabooApi {
    @Nullable
    private static Impl impl;

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

    public interface Impl {
        @Nullable
        EntityDisguiseHolder getDisguiseHolder(Entity entity);

        void onDisguiseChange(EntityDisguiseHolder holder, Entity entity);
    }
}
