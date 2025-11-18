package org.lovetropics.peekaboo.mixin.client;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.lovetropics.peekaboo.client.HasConditionalShadowRendering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;

import javax.annotation.Nullable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin implements HasConditionalShadowRendering {

    @Shadow
    private boolean shouldRenderShadow;

    @Unique
    @Nullable
    private Boolean lTMods$originalRenderShadow = null;

    @Override
    public void lTMods$setAndStoreOriginalRenderShadows(boolean newValue) {
        this.lTMods$originalRenderShadow = shouldRenderShadow;
        this.shouldRenderShadow = newValue;
    }

    @Override
    public void ltMods$restoreOriginalRenderShadows() {
        if(this.lTMods$originalRenderShadow != null) {
            this.shouldRenderShadow = lTMods$originalRenderShadow;
        }
        this.lTMods$originalRenderShadow = null;
    }
}
