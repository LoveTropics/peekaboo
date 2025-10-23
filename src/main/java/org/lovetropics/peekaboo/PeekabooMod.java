package org.lovetropics.peekaboo;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.lovetropics.peekaboo.api.Disguise;
import org.lovetropics.peekaboo.api.EntityDisguiseHolder;
import org.lovetropics.peekaboo.api.PeekabooApi;
import org.lovetropics.peekaboo.diguise.DisguiseBehavior;
import org.lovetropics.peekaboo.item.PeekabooItems;
import org.lovetropics.peekaboo.network.DisguiseSynchronizer;

import java.util.function.Supplier;

@Mod(PeekabooMod.ID)
public class PeekabooMod {
    public static final String ID = "peekaboo";

    private static final DeferredRegister<CreativeModeTab> TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);

    public static final DeferredHolder<CreativeModeTab, ?> CREATIVE_TAB = TAB_REGISTER.register("peekaboo", () -> CreativeModeTab.builder()
            .icon(() -> PeekabooItems.createDisguise(EntityType.CREEPER))
            .displayItems(PeekabooItems::fillCreativeTab)
            .title(Component.translatable("creative_tab.peekaboo"))
            .build());

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PeekabooMod.ID);

    public static final Supplier<AttachmentType<EntityDisguiseHolder>> ATTACHMENT = ATTACHMENT_TYPES.register(
            "disguise", () -> AttachmentType.builder(holder -> new EntityDisguiseHolder((LivingEntity) holder))
                    .serialize(new IAttachmentSerializer<>() {
                        @Override
                        public EntityDisguiseHolder read(IAttachmentHolder holder, ValueInput input) {
                            EntityDisguiseHolder disguise = new EntityDisguiseHolder((LivingEntity) holder);
                            disguise.set(input.read("disguise", Disguise.CODEC).orElse(Disguise.NONE));
                            return disguise;
                        }

                        @Override
                        public boolean write(EntityDisguiseHolder attachment, ValueOutput output) {
                            output.store("disguise", Disguise.CODEC, attachment.disguise());
                            return true;
                        }
                    }).build()
    );

    public PeekabooMod(IEventBus modBus) {
        PeekabooDataComponents.REGISTER.register(modBus);
        PeekabooItems.REGISTER.register(modBus);
        TAB_REGISTER.register(modBus);
        ATTACHMENT_TYPES.register(modBus);

        PeekabooApi.registerImpl(new PeekabooApi.Impl() {
            @Override
            @Nullable
            public EntityDisguiseHolder getDisguiseHolder(Entity entity) {
                if (entity instanceof LivingEntity) {
                    return entity.getData(ATTACHMENT);
                }
                return null;
            }

            @Override
            public void onDisguiseChange(EntityDisguiseHolder holder, Entity entity) {
                if (entity instanceof LivingEntity livingEntity) {
                    DisguiseBehavior.onDisguiseChange(livingEntity);
                    if (!entity.level().isClientSide()) {
                        DisguiseSynchronizer.broadcastDisguise(livingEntity);
                    }
                }
            }
        });
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }
}
