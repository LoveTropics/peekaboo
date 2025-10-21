package org.lovetropics.peekaboo;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lovetropics.peekaboo.diguise.Disguise;
import org.lovetropics.peekaboo.diguise.TypedEntityData;

public class PeekabooDataComponents {
    public static final DeferredRegister.DataComponents REGISTER = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, PeekabooMod.ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Disguise>> DISGUISE = REGISTER.registerComponentType(
            "disguise",
            builder -> builder.persistent(Disguise.CODEC).networkSynchronized(Disguise.STREAM_CODEC)
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TypedEntityData>> ENTITY = REGISTER.registerComponentType(
            "entity",
            builder -> builder.persistent(TypedEntityData.CODEC).networkSynchronized(TypedEntityData.STREAM_CODEC)
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> SIZE = REGISTER.registerComponentType(
            "size",
            builder -> builder.persistent(ExtraCodecs.POSITIVE_FLOAT).networkSynchronized(ByteBufCodecs.FLOAT)
    );
}
