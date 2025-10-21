package org.lovetropics.peekaboo.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.level.Level;
import org.lovetropics.peekaboo.diguise.EntityDisguiseHolder;
import org.lovetropics.peekaboo.diguise.DisguiseBehavior;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    @Final
    public WalkAnimationState walkAnimation;

    private LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "calculateEntityAnimation", at = @At("HEAD"), cancellable = true)
    private void calculateEntityAnimation(boolean includeHeight, CallbackInfo ci) {
        EntityDisguiseHolder disguise = EntityDisguiseHolder.getOrNull(this);
        if (disguise != null && disguise.entity() instanceof LivingEntity disguiseEntity) {
            disguiseEntity.calculateEntityAnimation(includeHeight);
            DisguiseBehavior.copyWalkAnimation(disguiseEntity.walkAnimation, walkAnimation);
            ci.cancel();
        }
    }
}
