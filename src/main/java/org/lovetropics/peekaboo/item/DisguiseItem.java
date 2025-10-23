package org.lovetropics.peekaboo.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import org.lovetropics.peekaboo.PeekabooDataComponents;
import org.lovetropics.peekaboo.PeekabooMod;
import org.lovetropics.peekaboo.api.Disguise;
import org.lovetropics.peekaboo.api.EntityDisguiseHolder;

@EventBusSubscriber(modid = PeekabooMod.ID)
public class DisguiseItem extends Item {
    public DisguiseItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Disguise disguise = stack.getOrDefault(PeekabooDataComponents.DISGUISE, Disguise.NONE);
        if (disguise.entity().isPresent()) {
            return Component.translatable(getDescriptionId() + ".entity", disguise.entity().get().type().getDescription());
        }
        return super.getName(stack);
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        EquipmentSlot slot = event.getSlot();
        Disguise fromDisguise = getDisguiseForSlot(event.getFrom(), entity, slot);
        Disguise toDisguise = getDisguiseForSlot(event.getTo(), entity, slot);
        if (!toDisguise.equals(fromDisguise)) {
            EntityDisguiseHolder.set(entity, toDisguise);
        }
    }

    private static Disguise getDisguiseForSlot(ItemStack item, LivingEntity entity, EquipmentSlot slot) {
        if (!item.canEquip(slot, entity)) {
            return Disguise.NONE;
        }
        return item.getOrDefault(PeekabooDataComponents.DISGUISE, Disguise.NONE);
    }
}
