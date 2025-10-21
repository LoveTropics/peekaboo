package org.lovetropics.peekaboo.diguise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;

public record Disguise(
        Optional<TypedEntityData> entity,
        float scale,
        boolean changesSize,
        Optional<Component> customName,
        Optional<ResolvableProfile> skinProfile
) {
    public static final Disguise NONE = new Disguise(
            Optional.empty(),
            1.0f,
            true,
            Optional.empty(),
            Optional.empty()
    );

    public static final Codec<Disguise> CODEC = RecordCodecBuilder.create(i -> i.group(
            TypedEntityData.CODEC.optionalFieldOf("entity").forGetter(Disguise::entity),
            Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(Disguise::scale),
            Codec.BOOL.optionalFieldOf("changes_size", true).forGetter(Disguise::changesSize),
            ComponentSerialization.CODEC.optionalFieldOf("custom_name").forGetter(Disguise::customName),
            ResolvableProfile.CODEC.optionalFieldOf("skin_profile").forGetter(Disguise::skinProfile)
    ).apply(i, Disguise::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Disguise> STREAM_CODEC = StreamCodec.composite(
            TypedEntityData.STREAM_CODEC.apply(ByteBufCodecs::optional), Disguise::entity,
            ByteBufCodecs.FLOAT, Disguise::scale,
            ByteBufCodecs.BOOL, Disguise::changesSize,
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs::optional), Disguise::customName,
            ResolvableProfile.STREAM_CODEC.apply(ByteBufCodecs::optional), Disguise::skinProfile,
            Disguise::new
    );

    public static Disguise of(EntityType<?> entity) {
        return Disguise.NONE.withEntity(Optional.of(new TypedEntityData(entity)));
    }

    @Nullable
    public Entity createEntity(Level level) {
        return entity.map(entity -> entity.createEntity(level)).orElse(null);
    }

    public boolean isEmpty() {
        return equals(NONE);
    }

    public Disguise clear(Disguise other) {
        Disguise clearTo = NONE;
        return new Disguise(
                entity.equals(other.entity) ? clearTo.entity : entity,
                scale == other.scale ? clearTo.scale : scale,
                changesSize == other.changesSize ? clearTo.changesSize : changesSize,
                customName.equals(other.customName) ? clearTo.customName : customName,
                skinProfile.equals(other.skinProfile) ? clearTo.skinProfile : skinProfile
        );
    }

    public Disguise withEntity(Optional<TypedEntityData> entity) {
        if (entity.equals(this.entity)) {
            return this;
        }
        return new Disguise(entity, scale, changesSize, customName, skinProfile);
    }

    public Disguise withScale(float scale) {
        if (scale == this.scale) {
            return this;
        }
        return new Disguise(entity, scale, changesSize, customName, skinProfile);
    }

    public Disguise withCustomName(Optional<Component> customName) {
        if (customName.equals(this.customName)) {
            return this;
        }
        return new Disguise(entity, scale, changesSize, customName, skinProfile);
    }

    public Disguise withSkinProfile(Optional<ResolvableProfile> skinProfile) {
        if (skinProfile.equals(this.skinProfile)) {
            return this;
        }
        return new Disguise(entity, scale, changesSize, customName, skinProfile);
    }
}
