package org.lovetropics.peekaboo.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lovetropics.peekaboo.PeekabooDataComponents;
import org.lovetropics.peekaboo.PeekabooMod;
import org.lovetropics.peekaboo.diguise.Disguise;
import org.lovetropics.peekaboo.diguise.TypedEntityData;

import java.util.function.Function;

public class PeekabooItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(PeekabooMod.ID);

    public static final DeferredItem<DisguiseItem> DISGUISE = REGISTER.registerItem("disguise", DisguiseItem::new, new Item.Properties()
            .stacksTo(1)
            .equippable(EquipmentSlot.HEAD));
    public static final DeferredItem<SimpleMobItem> MOB_HAT = REGISTER.registerItem("mob_hat", SimpleMobItem::new, new Item.Properties()
            .stacksTo(1)
            .equippable(EquipmentSlot.HEAD));
    public static final DeferredItem<SimpleMobItem> PLUSHIE = REGISTER.registerItem("plushie", SimpleMobItem::new, new Item.Properties()
            .stacksTo(1));

    public static void fillCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        fillForEntities(parameters, output, PeekabooItems::createDisguise);
        fillForEntities(parameters, output, entityType -> {
            ItemStack stack = MOB_HAT.toStack();
            stack.set(PeekabooDataComponents.ENTITY, new TypedEntityData(entityType));
            return stack;
        });
        fillForEntities(parameters, output, entityType -> {
            ItemStack stack = PLUSHIE.toStack();
            stack.set(PeekabooDataComponents.ENTITY, new TypedEntityData(entityType));
            return stack;
        });
    }

    private static void fillForEntities(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output, Function<EntityType<?>, ItemStack> itemFactory) {
        parameters.holders().lookupOrThrow(Registries.ENTITY_TYPE).listElements().forEach(entity -> {
            if (entity.value().getCategory() == MobCategory.MISC) {
                return;
            }
            output.accept(itemFactory.apply(entity.value()));
        });
    }

    public static ItemStack createDisguise(EntityType<?> entity) {
        ItemStack stack = DISGUISE.toStack();
        stack.set(PeekabooDataComponents.DISGUISE, Disguise.of(entity));
        return stack;
    }
}
