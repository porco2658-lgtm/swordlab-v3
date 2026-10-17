package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public class TriggerBot extends Module {
    private final Setting cooldown = add(Setting.number("Min cooldown %", 95, 50, 100, 1));
    private final Setting minDelay = add(Setting.number("Min delay (ticks)", 0, 0, 10, 1));
    private final Setting maxDelay = add(Setting.number("Max delay (ticks)", 2, 0, 10, 1));
    private final Setting playersOnly = add(Setting.bool("Players only", true));
    private int wait = -1;

    public TriggerBot() {
        super("Trigger Bot", "Attacks when the crosshair is on a target", Category.SWORD);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.gameMode == null || mc.screen != null || !holdingSword(mc)) { wait = -1; return; }
        if (!(mc.hitResult instanceof EntityHitResult hit)) { wait = -1; return; }

        Entity e = hit.getEntity();
        if (!(e instanceof LivingEntity le) || !le.isAlive()) { wait = -1; return; }
        if (playersOnly.bool && !(e instanceof Player)) { wait = -1; return; }
        if (p.getAttackStrengthScale(0f) < cooldown.value / 100.0) return;

        if (wait < 0) {
            int lo = minDelay.asInt(), hi = Math.max(lo, maxDelay.asInt());
            wait = lo + p.getRandom().nextInt(hi - lo + 1);
        }
        if (wait-- > 0) return;

        mc.gameMode.attack(p, e);
        p.swing(InteractionHand.MAIN_HAND);
        wait = -1;
    }
}
