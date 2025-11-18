package org.lovetropics.peekaboo.mixin.client;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin implements HasConditionalShadowRendering {

    @Shadow
    private boolean shouldRenderShadow;

    @Override
    public boolean lTMods$renderShadows() {
        return this.shouldRenderShadow;
    }
}
