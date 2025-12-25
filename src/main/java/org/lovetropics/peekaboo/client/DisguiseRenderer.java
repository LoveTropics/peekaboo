package org.lovetropics.peekaboo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import org.lovetropics.peekaboo.PeekabooMod;
import org.slf4j.Logger;

@EventBusSubscriber(modid = PeekabooMod.ID, value = Dist.CLIENT)
public class DisguiseRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void onRegisterRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier((Class<? extends LivingEntityRenderer<?, ?, ?>>) (Class<?>) LivingEntityRenderer.class, (entity, state) -> {
            DisguiseRenderState disguise = DisguiseRenderState.extract(entity, state);
            if (disguise != null) {
                state.setRenderData(DisguiseRenderState.KEY, disguise);
            }
        });
    }

    @SubscribeEvent
    public static void onRenderEntityPre(RenderLivingEvent.Pre<?, ?, ?> event) {
        DisguiseRenderState disguiseState = event.getRenderState().getRenderData(DisguiseRenderState.KEY);
        if (disguiseState == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();

        EntityRenderState disguiseEntityState = disguiseState.entityRenderState();
        float scale = disguiseState.scale();
        boolean hideShadow = disguiseState.hideShadow();

        if (disguiseEntityState != null) {
            int capturedTransformState = PoseStackCapture.get(poseStack);

            try {
                MultiBufferSource bufferSource = event.getMultiBufferSource();
                int packedLight = event.getPackedLight();

                poseStack.pushPose();
                poseStack.scale(scale, scale, scale);

                EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
                if(hideShadow){
                    dispatcher.setRenderShadow(false);
                }
                dispatcher.render(disguiseEntityState, 0.0, 0.0, 0.0, poseStack, bufferSource, packedLight);
                poseStack.popPose();
            } catch (Exception e) {
                LOGGER.error("Failed to render player disguise", e);
                PoseStackCapture.restore(poseStack, capturedTransformState);
            }

            event.setCanceled(true);

            // Big hack - the shadow was rendered by the disguise entity render call above, but canceling above does not discard the shadow
            event.getRenderState().isInvisible = true;
        } else {
            poseStack.pushPose();
            poseStack.scale(scale, scale, scale);
        }
    }

    @SubscribeEvent
    public static void onRenderEntityPost(RenderLivingEvent.Post<?, ?, ?> event) {
        DisguiseRenderState disguiseState = event.getRenderState().getRenderData(DisguiseRenderState.KEY);
        if (disguiseState != null) {
            Minecraft.getInstance().getEntityRenderDispatcher().setRenderShadow(true);
            if(disguiseState.entityRenderState() == null){
                event.getPoseStack().popPose();
            }
        }
    }
}
