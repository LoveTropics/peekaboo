package org.lovetropics.peekaboo.mixin;

import net.minecraft.world.entity.WalkAnimationState;
import org.lovetropics.peekaboo.duck.ExtendedWalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WalkAnimationState.class)
public class WalkAnimationStateMixin implements ExtendedWalkAnimationState {
    @Shadow
    private float positionScale;
    @Shadow
    private float position;
    @Shadow
    private float speed;
    @Shadow
    private float speedOld;

    @Override
    public void peekaboo$copyFrom(WalkAnimationState from) {
        WalkAnimationStateMixin shadowFrom = (WalkAnimationStateMixin) (Object) from;
        speed = shadowFrom.speed;
        speedOld = shadowFrom.speedOld;
        position = shadowFrom.position;
        positionScale = shadowFrom.positionScale;
    }
}
