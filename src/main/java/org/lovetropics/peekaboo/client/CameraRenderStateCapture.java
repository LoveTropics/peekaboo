package org.lovetropics.peekaboo.client;

import net.minecraft.client.renderer.state.CameraRenderState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

public class CameraRenderStateCapture {
    private static final Deque<CameraRenderState> STACK = new ArrayDeque<>();

    public static @Nullable CameraRenderState get() {
        return STACK.peek();
    }

    public static void push(CameraRenderState cameraRenderState) {
        STACK.push(cameraRenderState);
    }

    public static void pop() {
        STACK.pop();
    }
}
