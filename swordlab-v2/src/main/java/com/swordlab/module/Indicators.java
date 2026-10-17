package com.swordlab.module;

import com.swordlab.gui.Gfx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;

/** HUD panel with the health bar and armor of the player under your crosshair (or the nearest one). */
public class Indicators extends Module {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND};

    private final Setting range = add(Setting.number("Range", 12, 3, 40, 1));
    private final Setting armor = add(Setting.bool("Show armor", true));
    private final Setting xOff = add(Setting.number("X offset", 24, -300, 300, 4));
    private final Setting yOff = add(Setting.number("Y offset", 0, -200, 200, 4));
    private Player target;

    public Indicators() {
        super("Health & Armor", "HUD panel: target health + armor", Category.VISUALS);
    }

    @Override
    public void onDisable() { target = null; }

    @Override
    public void onTick(Minecraft mc) {
        target = null;
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;
        if (mc.hitResult instanceof EntityHitResult eh && eh.getEntity() instanceof Player t) { target = t; return; }
        double best = range.value * range.value;
        for (Player o : mc.level.players()) {
            if (o == p || o.isSpectator() || !o.isAlive()) continue;
            double d = p.distanceToSqr(o);
            if (d < best) { best = d; target = o; }
        }
    }

    public void render(GuiGraphics g, Minecraft mc) {
        Player t = target;
        if (!enabled || t == null || !t.isAlive() || mc.options.hideGui) return;
        Font f = mc.font;
        int w = 128, h = armor.bool ? 58 : 34;
        int x = g.guiWidth() / 2 + xOff.asInt();
        int y = g.guiHeight() / 2 - h / 2 + yOff.asInt();

        Gfx.rr(g, x, y, x + w, y + h, 10, 0x40101018, 0x58101018);
        Gfx.rr(g, x, y, x + w, y + h, 10, 0x24FFFFFF, 0x08FFFFFF);
        Gfx.border(g, x, y, x + w, y + h, 10, 0x40FFFFFF);

        g.drawString(f, t.getName().getString(), x + 8, y + 7, 0xFFFFFFFF, false);
        float hp = t.getHealth(), max = Math.max(1f, t.getMaxHealth()), abs = t.getAbsorptionAmount();
        String hs = String.format("%.1f", hp) + (abs > 0 ? " +" + (int) abs : "");
        g.drawString(f, hs, x + w - 8 - f.width(hs), y + 7, 0xFFFFFFFF, false);

        float ratio = Math.max(0f, Math.min(1f, hp / max));
        int bx1 = x + 8, bx2 = x + w - 8, by = y + 20;
        Gfx.rr(g, bx1, by, bx2, by + 6, 3, 0x33FFFFFF);
        int fx = bx1 + Math.round((bx2 - bx1) * ratio);
        if (fx - bx1 >= 2) Gfx.rr(g, bx1, by, fx, by + 6, 3, Gfx.lerp(0xFFFF3B30, 0xFF34C759, ratio));

        if (armor.bool) {
            int ix = x + 8;
            for (EquipmentSlot slot : SLOTS) {
                ItemStack s = t.getItemBySlot(slot);
                if (!s.isEmpty()) {
                    g.renderItem(s, ix, y + 34);
                    g.renderItemDecorations(f, s, ix, y + 34);
                }
                ix += 20;
            }
        }
    }
}
