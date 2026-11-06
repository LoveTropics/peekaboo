package org.lovetropics.peekaboo.mixin.client;

import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(ClientMannequin.class)
public abstract class ClientMannequinMixin {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    private @Nullable CompletableFuture<Optional<PlayerSkin>> skinLookup;

    @Shadow
    protected abstract void setSkin(PlayerSkin skin);

    @Inject(method = "getSkin", at = @At("HEAD"))
    private void getSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        if (skinLookup != null && skinLookup.isDone()) {
            try {
                skinLookup.get().ifPresent(this::setSkin);
                skinLookup = null;
            } catch (Exception e) {
                LOGGER.error("Error when trying to look up skin", e);
            }
        }
    }
}
