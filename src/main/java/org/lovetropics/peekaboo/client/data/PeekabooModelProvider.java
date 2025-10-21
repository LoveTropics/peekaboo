package org.lovetropics.peekaboo.client.data;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import org.lovetropics.peekaboo.PeekabooMod;
import org.lovetropics.peekaboo.client.item.MobItemSpecialRenderer;
import org.lovetropics.peekaboo.item.PeekabooItems;

import java.util.Optional;

@EventBusSubscriber(modid = PeekabooMod.ID, value = Dist.CLIENT)
public class PeekabooModelProvider extends ModelProvider {
    private static final ResourceLocation DISGUISE_ITEM_SPRITE = PeekabooMod.location("item/disguise");
    private static final ResourceLocation MOB_HAT_SPRITE = PeekabooMod.location("item/mob_hat");
    private static final ResourceLocation PLUSHIE_SPRITE = PeekabooMod.location("item/plushie");

    public PeekabooModelProvider(PackOutput output) {
        super(output, PeekabooMod.ID);
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        event.createProvider(PeekabooModelProvider::new);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        generateMobItem(PeekabooItems.DISGUISE, itemModels, MobItemSpecialRenderer.EntitySource.DISGUISE, Optional.of(DISGUISE_ITEM_SPRITE));
        generateMobItem(PeekabooItems.MOB_HAT, itemModels, MobItemSpecialRenderer.EntitySource.ENTITY, Optional.of(MOB_HAT_SPRITE));
        generateMobItem(PeekabooItems.PLUSHIE, itemModels, MobItemSpecialRenderer.EntitySource.ENTITY, Optional.of(PLUSHIE_SPRITE));
    }

    private static void generateMobItem(DeferredItem<?> item, ItemModelGenerators itemModels, MobItemSpecialRenderer.EntitySource entitySource, Optional<ResourceLocation> inventorySprite) {
        ResourceLocation baseModel = ModelTemplates.PARTICLE_ONLY.create(item.get(), TextureMapping.particle(Blocks.BLACK_WOOL), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item.get(), ItemModelUtils.specialModel(baseModel, new MobItemSpecialRenderer.Unbaked(entitySource, inventorySprite)));
    }
}
