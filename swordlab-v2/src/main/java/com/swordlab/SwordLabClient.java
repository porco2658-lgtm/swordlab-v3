package com.swordlab;

import com.mojang.blaze3d.platform.InputConstants;
import com.swordlab.gui.ClickGui;
import com.swordlab.module.AimState;
import com.swordlab.module.Indicators;
import com.swordlab.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class SwordLabClient implements ClientModInitializer {
    private static KeyMapping openKey;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();

        KeyMapping.Category category =
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath("swordlab", "main"));
        openKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.swordlab.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));

        ClientTickEvents.START_CLIENT_TICK.register(mc -> {
            while (openKey.consumeClick()) {
                if (mc.screen == null) mc.setScreen(new ClickGui());
            }
            if (mc.player == null || mc.level == null) {
                AimState.active = false;
                return;
            }
            ModuleManager.tick(mc);
        });

        HudRenderCallback.EVENT.register((graphics, delta) -> {
            Indicators i = ModuleManager.INDICATORS;
            if (i != null) i.render(graphics, Minecraft.getInstance());
        });
    }
}
