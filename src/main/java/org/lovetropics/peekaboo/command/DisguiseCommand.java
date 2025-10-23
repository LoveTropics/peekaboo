package org.lovetropics.peekaboo.command;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.lovetropics.peekaboo.PeekabooMod;
import org.lovetropics.peekaboo.api.Disguise;
import org.lovetropics.peekaboo.api.EntityDisguiseHolder;
import org.lovetropics.peekaboo.api.TypedEntityData;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.ComponentArgument.getResolvedComponent;
import static net.minecraft.commands.arguments.ComponentArgument.textComponent;
import static net.minecraft.commands.arguments.CompoundTagArgument.compoundTag;
import static net.minecraft.commands.arguments.CompoundTagArgument.getCompoundTag;
import static net.minecraft.commands.arguments.GameProfileArgument.gameProfile;
import static net.minecraft.commands.arguments.GameProfileArgument.getGameProfiles;
import static net.minecraft.commands.arguments.ResourceArgument.getSummonableEntityType;

@EventBusSubscriber(modid = PeekabooMod.ID)
public class DisguiseCommand {
    private static final SimpleCommandExceptionType NOT_LIVING_ENTITY = new SimpleCommandExceptionType(Component.translatable("commands.disguise.not_living_entity"));

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal("disguise")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(literal("as")
                        .then(argument("entity", ResourceArgument.resource(event.getBuildContext(), Registries.ENTITY_TYPE))
                                .suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES))
                                .executes(context -> disguiseAsEntity(context, getSummonableEntityType(context, "entity"), new CompoundTag()))
                                .then(argument("nbt", compoundTag())
                                        .executes(context -> disguiseAsEntity(context, getSummonableEntityType(context, "entity"), getCompoundTag(context, "nbt")))
                                )
                        )
                )
                .then(literal("skin")
                        .then(literal("as")
                                .then(argument("player", gameProfile())
                                        .executes(context -> disguiseSkin(context, getGameProfiles(context, "player")))
                                )
                        )
                        .then(literal("clear")
                                .executes(DisguiseCommand::clearSkin)
                        )
                )
                .then(literal("name")
                        .then(literal("as")
                                .then(argument("name", textComponent(event.getBuildContext()))
                                        .executes(context -> disguiseName(context, getResolvedComponent(context, "name")))
                                )
                        )
                        .then(literal("clear")
                                .executes(DisguiseCommand::clearName)
                        )
                )
                .then(literal("scale")
                        .then(argument("scale", floatArg(0.1f, 20.0f))
                                .executes(context -> disguiseScale(context, getFloat(context, "scale")))
                        )
                )
                .then(literal("clear")
                        .executes(DisguiseCommand::clearDisguise)
                )
        );
    }

    private static int disguiseAsEntity(CommandContext<CommandSourceStack> context, Holder.Reference<EntityType<?>> entity, CompoundTag nbt) throws CommandSyntaxException {
        TypedEntityData entityData = new TypedEntityData(entity.value(), nbt);
        return updateDisguise(context, disguise -> disguise.withEntity(Optional.of(entityData)));
    }

    private static int disguiseSkin(CommandContext<CommandSourceStack> context, Collection<GameProfile> profiles) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (profiles.size() != 1) {
            throw EntityArgument.ERROR_NOT_SINGLE_PLAYER.create();
        }

        GameProfile sourceProfile = Iterables.getOnlyElement(profiles);
        CompletableFuture<ResolvableProfile> future = SkullBlockEntity.fetchGameProfile(sourceProfile.getId())
                .thenApply(maybeProfile -> new ResolvableProfile(maybeProfile.orElse(sourceProfile)));

        if (future.isDone()) {
            EntityDisguiseHolder.update(player, disguise -> disguise.withSkinProfile(Optional.of(future.join())));
            return 1;
        }

        // Just put something in there for now, and replace it later
        Disguise temporaryDisguise = EntityDisguiseHolder.update(player, disguise -> disguise.withSkinProfile(Optional.of(new ResolvableProfile(sourceProfile))));
        future.thenAcceptAsync(
                resolvedProfile -> EntityDisguiseHolder.update(player, replacedDisguise -> {
                    // The skin changed again before we resolved it, don't replace
                    if (!Objects.equals(replacedDisguise.skinProfile(), temporaryDisguise.skinProfile())) {
                        return replacedDisguise;
                    }
                    return replacedDisguise.withSkinProfile(Optional.of(resolvedProfile));
                }),
                context.getSource().getServer()
        );

        return 1;
    }

    private static int clearSkin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return updateDisguise(context, d -> d.withSkinProfile(Optional.empty()));
    }

    private static int disguiseName(CommandContext<CommandSourceStack> context, Component name) throws CommandSyntaxException {
        return updateDisguise(context, d -> d.withCustomName(Optional.of(name)));
    }

    private static int clearName(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return updateDisguise(context, d -> d.withCustomName(Optional.empty()));
    }

    private static int disguiseScale(CommandContext<CommandSourceStack> context, float scale) throws CommandSyntaxException {
        return updateDisguise(context, d -> d.withScale(scale));
    }

    private static int clearDisguise(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return updateDisguise(context, d -> Disguise.NONE);
    }

    private static int updateDisguise(CommandContext<CommandSourceStack> context, UnaryOperator<Disguise> disguise) throws CommandSyntaxException {
        Entity entity = context.getSource().getEntityOrException();
        if (!(entity instanceof LivingEntity livingEntity)) {
            throw NOT_LIVING_ENTITY.create();
        }
        EntityDisguiseHolder.update(livingEntity, disguise);
        return 1;
    }
}
