package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Raises the client-side entity interaction range (vanilla = 3.0). The server still enforces its own
 * limit (about 4.0 on vanilla), so values above that are rejected by vanilla servers - good for testing
 * where your anti-cheat's reach check actually triggers.
 */
public class Reach extends Module {
    private final Setting reach = add(Setting.number("Reach", 3.5, 3.0, 4.5, 0.1));

    public Reach() {
        super("Reach", "Extends attack range (3.0 - 4.5 blocks)", Category.SWORD);
    }

    @Override
    public void onTick(Minecraft mc) {
        apply(mc.player, holdingSword(mc) ? reach.value : 3.0);
    }

    @Override
    public void onDisable() { apply(Minecraft.getInstance().player, 3.0); }

    private static void apply(LocalPlayer p, double v) {
        if (p == null) return;
        AttributeInstance a = p.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (a != null && a.getBaseValue() != v) a.setBaseValue(v);
    }
}
