package org.lovetropics.peekaboo.client.data;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.data.PackOutput;
import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import org.lovetropics.peekaboo.PeekabooMod;
import org.lovetropics.peekaboo.client.item.MobItemSpecialRenderer;
import org.lovetropics.peekaboo.item.PeekabooItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = PeekabooMod.ID, value = Dist.CLIENT)
public class PeekabooModelProvider extends ModelProvider {
    private static final Identifier DISGUISE_ITEM_SPRITE = PeekabooMod.identifier("item/disguise");
    private static final Identifier MOB_HAT_SPRITE = PeekabooMod.identifier("item/mob_hat");
    private static final Identifier PLUSHIE_SPRITE = PeekabooMod.identifier("item/plushie");

    public PeekabooModelProvider(PackOutput output) {
        super(output, PeekabooMod.ID);
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        event.createProvider(PeekabooModelProvider::new);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        generateMobItem(PeekabooItems.DISGUISE, itemModels, MobItemSpecialRenderer.EntitySource.DISGUISE, DISGUISE_ITEM_SPRITE);
        generateMobItem(PeekabooItems.MOB_HAT, itemModels, MobItemSpecialRenderer.EntitySource.ENTITY, MOB_HAT_SPRITE);
        generateMobItem(PeekabooItems.PLUSHIE, itemModels, MobItemSpecialRenderer.EntitySource.ENTITY, PLUSHIE_SPRITE);
    }

    private static void generateMobItem(DeferredItem<?> item, ItemModelGenerators itemModels, MobItemSpecialRenderer.EntitySource entitySource, Identifier inventorySprite) {
        Identifier baseModel = ModelTemplates.PARTICLE_ONLY.create(item.get(), TextureMapping.particle(Blocks.WOOL.black()), itemModels.modelOutput);
        ItemModel.Unbaked groundModel = ItemModelUtils.specialModel(baseModel, new MobItemSpecialRenderer.Unbaked(entitySource, Optional.empty(), ItemDisplayContext.GROUND));

        List<SelectItemModel.SwitchCase<ItemDisplayContext>> cases = getSwitchCases(entitySource, inventorySprite, baseModel);
        ItemModel.Unbaked select = ItemModelUtils.select(new DisplayContext(), groundModel, cases.toArray(new SelectItemModel.SwitchCase[0]));

        itemModels.itemModelOutput.accept(item.get(), select);
    }

    private static List<SelectItemModel.SwitchCase<ItemDisplayContext>> getSwitchCases(MobItemSpecialRenderer.EntitySource entitySource, Identifier inventorySprite, Identifier baseModel) {
        List<SelectItemModel.SwitchCase<ItemDisplayContext>> cases = new ArrayList<>();

        for (ItemDisplayContext value : ItemDisplayContext.values()) {
            if (value == ItemDisplayContext.GROUND || value == ItemDisplayContext.NONE) {
                continue;
            }
            Optional<Identifier> texture = value == ItemDisplayContext.GUI ? Optional.of(inventorySprite) : Optional.empty();
            cases.add(ItemModelUtils.when(value, ItemModelUtils.specialModel(baseModel, new MobItemSpecialRenderer.Unbaked(entitySource, texture, value))));
        }
        return cases;
    }
}
