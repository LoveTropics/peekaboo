package org.lovetropics.peekaboo.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lovetropics.peekaboo.PeekabooDataComponents;
import org.lovetropics.peekaboo.diguise.TypedEntityData;

public class SimpleMobItem extends Item {
    public SimpleMobItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        TypedEntityData entity = stack.get(PeekabooDataComponents.ENTITY);
        if (entity != null) {
            return Component.translatable(getDescriptionId() + ".entity", entity.type().getDescription());
        }
        return super.getName(stack);
    }
}
