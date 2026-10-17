package com.swordlab.gui;

import net.minecraft.client.gui.GuiGraphics;

/** Tiny 2D helpers: rounded translucent rects, 1px rounded borders, color lerp. */
public final class Gfx {
    private Gfx() {}

    public static int lerp(int a, int b, float t) {
        int out = 0;
        for (int s = 24; s >= 0; s -= 8) {
            int ca = (a >>> s) & 255, cb = (b >>> s) & 255;
            out |= ((int) (ca + (cb - ca) * t) & 255) << s;
        }
        return out;
    }

    private static int inset(int i, int h, int r) {
        if (r <= 0) return 0;
        int j = i < r ? i : (i >= h - r ? h - 1 - i : -1);
        if (j < 0) return 0;
        double dy = r - j - 0.5;
        return r - (int) Math.round(Math.sqrt(Math.max(0, (double) r * r - dy * dy)));
    }

    private static int clampR(int x1, int y1, int x2, int y2, int r) {
        return Math.max(0, Math.min(r, Math.min(y2 - y1, x2 - x1) / 2));
    }

    /** Vertical-gradient rounded rect (one 1px row at a time, no overlap so alpha stays clean). */
    public static void rr(GuiGraphics g, int x1, int y1, int x2, int y2, int r, int top, int bot) {
        int h = y2 - y1;
        r = clampR(x1, y1, x2, y2, r);
        for (int i = 0; i < h; i++) {
            int ins = inset(i, h, r);
            g.fill(x1 + ins, y1 + i, x2 - ins, y1 + i + 1, lerp(top, bot, h <= 1 ? 0f : (float) i / (h - 1)));
        }
    }

    public static void rr(GuiGraphics g, int x1, int y1, int x2, int y2, int r, int color) {
        rr(g, x1, y1, x2, y2, r, color, color);
    }

    public static void border(GuiGraphics g, int x1, int y1, int x2, int y2, int r, int c) {
        int h = y2 - y1;
        r = clampR(x1, y1, x2, y2, r);
        for (int i = 0; i < h; i++) {
            int ins = inset(i, h, r);
            if (i == 0 || i == h - 1) {
                g.fill(x1 + ins, y1 + i, x2 - ins, y1 + i + 1, c);
                continue;
            }
            int w;
            if (i < r) w = Math.max(1, inset(i - 1, h, r) - ins);
            else if (i >= h - r) w = Math.max(1, inset(i + 1, h, r) - ins);
            else w = 1;
            g.fill(x1 + ins, y1 + i, x1 + ins + w, y1 + i + 1, c);
            g.fill(x2 - ins - w, y1 + i, x2 - ins, y1 + i + 1, c);
        }
    }
}
