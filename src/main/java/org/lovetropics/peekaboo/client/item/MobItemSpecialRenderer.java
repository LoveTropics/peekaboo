package org.lovetropics.peekaboo.client.item;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.lovetropics.peekaboo.PeekabooDataComponents;
import org.lovetropics.peekaboo.api.Disguise;
import org.lovetropics.peekaboo.api.TypedEntityData;
import org.lovetropics.peekaboo.client.DisguiseRenderState;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class MobItemSpecialRenderer implements SpecialModelRenderer<MobItemSpecialRenderer.Argument> {
    private final Minecraft minecraft;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final EntityInfoCache entityInfoCache;
    @Nullable
    private final ResourceLocation inventorySprite;

    private MobItemSpecialRenderer(Minecraft minecraft, EntitySource entitySource, @Nullable ResourceLocation inventorySprite) {
        this.minecraft = minecraft;
        entityRenderDispatcher = minecraft.getEntityRenderDispatcher();
        entityInfoCache = new EntityInfoCache(entityRenderDispatcher, entitySource);
        this.inventorySprite = inventorySprite;
    }

    private static void addVertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float u, float v, int packedLight, int packedOverlay) {
        consumer.addVertex(pose, x, y, 0.0f)
                .setColor(1.0f, 1.0f, 1.0f, 1.0f)
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    private void drawInventorySprite(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (inventorySprite == null) {
            return;
        }
        ResourceLocation atlas = TextureAtlas.LOCATION_BLOCKS;
        TextureAtlasSprite sprite = minecraft.getTextureAtlas(atlas).apply(inventorySprite);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.textSeeThrough(atlas));
        PoseStack.Pose pose = poseStack.last();
        addVertex(consumer, pose, 0.0f, 0.0f, sprite.getU0(), sprite.getV1(), packedLight, packedOverlay);
        addVertex(consumer, pose, 1.0f, 0.0f, sprite.getU1(), sprite.getV1(), packedLight, packedOverlay);
        addVertex(consumer, pose, 1.0f, 1.0f, sprite.getU1(), sprite.getV0(), packedLight, packedOverlay);
        addVertex(consumer, pose, 0.0f, 1.0f, sprite.getU0(), sprite.getV0(), packedLight, packedOverlay);
    }

    @Override
    public void render(@Nullable Argument argument, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean hasFoilType) {
        if (argument != null) {
            drawEntity(argument, displayContext, poseStack, bufferSource, packedLight);
        }
        if (displayContext == ItemDisplayContext.GUI) {
            drawInventorySprite(poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private void drawEntity(Argument argument, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        applyTransforms(argument, displayContext, poseStack);

        entityRenderDispatcher.render(argument.entity.renderState(), 0.0, 0.0, 0.0, poseStack, bufferSource, packedLight);

        poseStack.popPose();
    }

    private void applyTransforms(Argument argument, ItemDisplayContext context, PoseStack poseStack) {
        float scale = getScale(argument, context);
        poseStack.scale(scale, scale, scale);

        boolean left = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        switch (context) {
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(left ? 25.0f : -25.0f));
                poseStack.translate(0.0f, -0.2f / scale, 0.0f);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(left ? 45.0f : -45.0f));
                poseStack.translate(0.0f, -0.1f / scale, -argument.entity.width() / 2.0f);
            }
            case HEAD -> {
                poseStack.translate(0.0f, 0.375f / scale, 0.0f);
                poseStack.mulPose(Axis.YP.rotation(Mth.PI));
            }
            case GUI -> {
                poseStack.translate(0.0f, -argument.entity.height() / 2.0f, 0.0f);
                poseStack.mulPose(Axis.XP.rotationDegrees(25.0f));
                poseStack.mulPose(Axis.YP.rotationDegrees(315.0f));
            }
            case GROUND -> poseStack.translate(0.0f, -0.2f / scale, 0.0f);
            case FIXED -> {
                poseStack.translate(0.0f, -0.2f / scale - argument.entity.height() / 2.0f, 0.0f);
                poseStack.mulPose(Axis.YP.rotation(Mth.PI));
            }
        }
    }

    private float getScale(Argument argument, ItemDisplayContext context) {
        float targetSize = argument.targetSize() * switch (context) {
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND ->
                    0.8f;
            case HEAD -> 1.5f;
            case GUI, FIXED -> 0.9f;
            case GROUND -> 0.5f;
            default -> 1.0f;
        };
        return targetSize / Math.max(argument.entity.approximateSize(), 1.0f);
    }

    @Override
    public void getExtents(Set<Vector3f> output) {
        output.add(new Vector3f(0.0f, 0.0f, 0.0f));
        output.add(new Vector3f(1.0f, 0.0f, 0.0f));
        output.add(new Vector3f(0.0f, 1.0f, 0.0f));
        output.add(new Vector3f(1.0f, 1.0f, 0.0f));
        output.add(new Vector3f(0.0f, 0.0f, 1.0f));
        output.add(new Vector3f(1.0f, 0.0f, 1.0f));
        output.add(new Vector3f(0.0f, 1.0f, 1.0f));
        output.add(new Vector3f(1.0f, 1.0f, 1.0f));
    }

    @Override
    @Nullable
    public Argument extractArgument(ItemStack stack) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            return null;
        }
        ExtractedEntity info = entityInfoCache.get(level, stack);
        if (info == null) {
            return null;
        }
        float targetSize = stack.getOrDefault(PeekabooDataComponents.SIZE, 1.0f);
        return new Argument(info, targetSize);
    }

    private static class EntityInfoCache {
        private final EntityRenderDispatcher entityRenderDispatcher;
        private final EntitySource entitySource;

        @Nullable
        private WeakReference<ClientLevel> level;
        private final Cache<TypedEntityData, EntityInfo> entities = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofSeconds(10)).build();

        private EntityInfoCache(EntityRenderDispatcher entityRenderDispatcher, EntitySource entitySource) {
            this.entityRenderDispatcher = entityRenderDispatcher;
            this.entitySource = entitySource;
        }

        @Nullable
        public ExtractedEntity get(ClientLevel level, ItemStack itemStack) {
            TypedEntityData type = entitySource.get(itemStack);
            if (type == null) {
                return null;
            }
            if (this.level == null || this.level.get() != level) {
                entities.invalidateAll();
                this.level = new WeakReference<>(level);
            }
            EntityInfo entityInfo = entities.getIfPresent(type);
            if (entityInfo == null) {
                Entity entity = type.createEntity(level);
                entityInfo = new EntityInfo(entity);
                entities.put(type, entityInfo);
            }
            return entityInfo.getOrExtractEntity(entityRenderDispatcher);
        }
    }

    private static class EntityInfo {
        private static final long EXPIRE_AFTER_MILLIS = 1000;

        private final @Nullable Entity entity;
        private @Nullable ExtractedEntity extractedEntity;
        private long extractedAtTime;

        private EntityInfo(@Nullable Entity entity) {
            this.entity = entity;
        }

        private boolean hasExtractionExpired() {
            return Util.getMillis() >= extractedAtTime + EXPIRE_AFTER_MILLIS;
        }

        @Nullable
        public ExtractedEntity getOrExtractEntity(EntityRenderDispatcher entityRenderDispatcher) {
            // This kind of sucks, but some entities might update after being created, for example Dummy Players resolving skins
            // Otherwise, we could discard the entity instance entirely after extracting
            if (extractedEntity != null && !hasExtractionExpired()) {
                return extractedEntity;
            }
            if (entity == null) {
                return null;
            }
            extractedAtTime = Util.getMillis();
            extractedEntity = extractEntity(entityRenderDispatcher, entity);
            return extractedEntity;
        }

        private <E extends Entity> ExtractedEntity extractEntity(EntityRenderDispatcher entityRenderDispatcher, E entity) {
            EntityRenderer<? super E, ?> renderer = entityRenderDispatcher.getRenderer(entity);
            return new ExtractedEntity(
                    DisguiseRenderState.createFreshRenderState(renderer, entity, 1.0f),
                    entity.getBbWidth(),
                    entity.getBbHeight(),
                    // Approximate size of the entity - overestimate width a bit because bounding boxes are usually too small
                    Math.max(entity.getBbWidth() * 2.0f, entity.getBbHeight())
            );
        }
    }

    public record ExtractedEntity(
            // Note: render states are only identity-compared, so the items will be redrawn in UI whenever we refresh the render state
            EntityRenderState renderState,
            float width,
            float height,
            float approximateSize
    ) {
    }

    public record Argument(
            ExtractedEntity entity,
            float targetSize
    ) {
    }

    public record Unbaked(
            EntitySource entitySource,
            Optional<ResourceLocation> inventorySprite
    ) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                EntitySource.CODEC.fieldOf("entity_source").forGetter(Unbaked::entitySource),
                ResourceLocation.CODEC.optionalFieldOf("inventory_sprite").forGetter(Unbaked::inventorySprite)
        ).apply(i, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet modelSet) {
            return new MobItemSpecialRenderer(Minecraft.getInstance(), entitySource, inventorySprite.orElse(null));
        }
    }

    public enum EntitySource implements StringRepresentable {
        DISGUISE("disguise", stack -> {
            Disguise disguise = stack.getOrDefault(PeekabooDataComponents.DISGUISE, Disguise.NONE);
            return disguise.entity().orElse(null);
        }),
        ENTITY("entity", stack -> stack.get(PeekabooDataComponents.ENTITY)),
        ;

        public static final Codec<EntitySource> CODEC = StringRepresentable.fromEnum(EntitySource::values);

        private final String name;
        private final Function<ItemStack, TypedEntityData> extractor;

        EntitySource(String name, Function<ItemStack, TypedEntityData> extractor) {
            this.name = name;
            this.extractor = extractor;
        }

        @Nullable
        public TypedEntityData get(ItemStack stack) {
            return extractor.apply(stack);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
