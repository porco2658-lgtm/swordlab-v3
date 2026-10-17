package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class JumpReset extends Module {
    private final Setting trigger = add(Setting.number("Trigger hurtTime", 10, 1, 10, 1));
    private final Setting chance = add(Setting.number("Chance %", 100, 0, 100, 5));
    private boolean pressed;

    public JumpReset() {
        super("Auto Jump Reset", "Jumps right after taking a hit to cut knockback", Category.SWORD);
    }

    @Override
    public void onDisable() { release(Minecraft.getInstance()); }

    private void release(Minecraft mc) {
        if (pressed) { mc.options.keyJump.setDown(false); pressed = false; }
    }

    @Override
    public void onTick(Minecraft mc) {
        release(mc);
        LocalPlayer p = mc.player;
        if (p == null || mc.screen != null || !holdingSword(mc)) return;
        if (p.hurtTime == trigger.asInt() && p.onGround()
                && p.getRandom().nextInt(100) < chance.asInt()) {
            mc.options.keyJump.setDown(true);
            pressed = true;
        }
    }
}
