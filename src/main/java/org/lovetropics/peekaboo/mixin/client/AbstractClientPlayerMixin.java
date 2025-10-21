package org.lovetropics.peekaboo.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import org.lovetropics.peekaboo.diguise.Disguise;
import org.lovetropics.peekaboo.diguise.EntityDisguiseHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player {
    public AbstractClientPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private void getSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        Disguise disguise = EntityDisguiseHolder.getDisguise(this);
        Optional<ResolvableProfile> skinProfile = disguise.skinProfile();
        if (skinProfile.isPresent()) {
            SkinManager skinManager = Minecraft.getInstance().getSkinManager();
            cir.setReturnValue(skinManager.getInsecureSkin(skinProfile.get().gameProfile()));
        }
    }
}
