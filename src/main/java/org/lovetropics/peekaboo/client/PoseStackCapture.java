package org.lovetropics.peekaboo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import org.lovetropics.peekaboo.mixin.client.PoseStackAccessor;

public class PoseStackCapture {
    private PoseStackCapture() {
    }

    public static int get(PoseStack poseStack) {
        return ((PoseStackAccessor) poseStack).getLastIndex();
    }

    public static void restore(PoseStack poseStack, int lastIndex) {
        ((PoseStackAccessor) poseStack).setLastIndex(lastIndex);
    }
}
