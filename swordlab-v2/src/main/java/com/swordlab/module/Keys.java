package com.swordlab.module;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/** Key helpers. If a Minecraft update changes the InputConstants call, fix it here only. */
public final class Keys {
    private Keys() {}

    public static boolean down(int key) {
        if (key < 0) return false;
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), key);
    }

    /** Number keys 1-9 -> hotbar index 0-8, anything else -> -1. */
    public static int slot(int key) {
        return key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_9 ? key - GLFW.GLFW_KEY_1 : -1;
    }

    public static String name(int key) {
        return key < 0 ? "None" : InputConstants.Type.KEYSYM.getOrCreate(key).getDisplayName().getString();
    }
}
