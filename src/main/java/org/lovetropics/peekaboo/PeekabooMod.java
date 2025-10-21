package org.lovetropics.peekaboo;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lovetropics.peekaboo.diguise.EntityDisguiseHolder;
import org.lovetropics.peekaboo.item.PeekabooItems;

@Mod(PeekabooMod.ID)
public class PeekabooMod {
    public static final String ID = "peekaboo";

    private static final DeferredRegister<CreativeModeTab> TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);

    public static final DeferredHolder<CreativeModeTab, ?> CREATIVE_TAB = TAB_REGISTER.register("peekaboo", () -> CreativeModeTab.builder()
            .icon(() -> PeekabooItems.createDisguise(EntityType.CREEPER))
            .displayItems(PeekabooItems::fillCreativeTab)
            .title(Component.translatable("creative_tab.peekaboo"))
            .build());

    public PeekabooMod(IEventBus modBus) {
        EntityDisguiseHolder.ATTACHMENT_TYPES.register(modBus);
        PeekabooDataComponents.REGISTER.register(modBus);
        PeekabooItems.REGISTER.register(modBus);
        TAB_REGISTER.register(modBus);
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }
}
