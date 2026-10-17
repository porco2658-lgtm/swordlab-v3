package com.swordlab.gui;

import com.swordlab.module.Category;
import com.swordlab.module.Keys;
import com.swordlab.module.Module;
import com.swordlab.module.ModuleManager;
import com.swordlab.module.Setting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** "Liquid glass" click GUI: near-transparent panel, soft highlights, iOS-style switches. */
public class ClickGui extends Screen {
    private static final int W = 400, H = 250, R = 14, SIDEBAR = 96;
    private static final int WHITE = 0xFFFFFFFF, DIM = 0x99FFFFFF, FAINT = 0x66FFFFFF;

    private enum Kind { TAB, TOGGLE, ARROW, BOOL, SLIDER, KEY, MODE }

    private record Hit(int x1, int y1, int x2, int y2, Kind kind, Object ref) {
        boolean in(double x, double y) { return x >= x1 && x < x2 && y >= y1 && y < y2; }
    }

    private final List<Hit> hits = new ArrayList<>();
    private Category category = Category.SWORD;
    private Hit dragging;
    private Setting listening; // key setting waiting for a key press
    private float scroll;
    private int contentH, cy1, cy2;

    public ClickGui() { super(Component.literal("SwordLab")); }

    @Override public boolean isPauseScreen() { return false; }

    // No vanilla dim/blur: the world stays visible through the glass.
    @Override public void renderBackground(GuiGraphics g, int mx, int my, float pt) {}

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        hits.clear();
        int px = (width - W) / 2, py = (height - H) / 2;

        // Glass panel: faint dark tint, white sheen, thin lit edge.
        Gfx.rr(g, px, py, px + W, py + H, R, 0x30101018, 0x4A101018);
        Gfx.rr(g, px, py, px + W, py + H, R, 0x2CFFFFFF, 0x08FFFFFF);
        Gfx.border(g, px, py, px + W, py + H, R, 0x4DFFFFFF);
        g.fill(px + R, py + 1, px + W - R, py + 2, 0x40FFFFFF);
        g.fill(px + SIDEBAR, py + 14, px + SIDEBAR + 1, py + H - 14, 0x1AFFFFFF);

        // Sidebar
        text(g, "SwordLab", px + 16, py + 14, WHITE);
        text(g, "test client v2", px + 16, py + 26, FAINT);
        int ty = py + 46;
        for (Category c : Category.values()) {
            int x1 = px + 8, x2 = px + SIDEBAR - 8;
            if (c == category) {
                Gfx.rr(g, x1, ty, x2, ty + 22, 8, 0x2EFFFFFF, 0x14FFFFFF);
                Gfx.border(g, x1, ty, x2, ty + 22, 8, 0x35FFFFFF);
            }
            text(g, c.label, x1 + 10, ty + 7, c == category ? WHITE : DIM);
            hits.add(new Hit(x1, ty, x2, ty + 22, Kind.TAB, c));
            ty += 26;
        }
        text(g, "RClick: settings", px + 14, py + H - 32, FAINT);
        text(g, "RShift: close", px + 14, py + H - 22, FAINT);

        // Content
        int cx1 = px + SIDEBAR + 8, cx2 = px + W - 12;
        cy1 = py + 14;
        cy2 = py + H - 12;
        scroll = Math.max(0, Math.min(scroll, Math.max(0, contentH - (cy2 - cy1))));

        g.enableScissor(cx1 - 2, cy1, cx2 + 2, cy2);
        int y = cy1 - (int) scroll, total = 0;
        for (Module m : ModuleManager.in(category)) {
            int h = 26 + (m.expanded ? 14 + m.settings.size() * 24 + 4 : 0);
            m.anim += ((m.enabled ? 1f : 0f) - m.anim) * 0.35f;

            Gfx.rr(g, cx1, y, cx2, y + h, 9, 0x20FFFFFF, 0x0DFFFFFF);
            Gfx.border(g, cx1, y, cx2, y + h, 9, 0x30FFFFFF);
            text(g, m.expanded ? "v" : ">", cx1 + 9, y + 9, DIM);
            text(g, m.name, cx1 + 22, y + 9, WHITE);
            pill(g, cx2 - 36, y + 7, m.anim);
            hits.add(new Hit(cx1, y, cx1 + 20, y + 26, Kind.ARROW, m));
            hits.add(new Hit(cx1 + 20, y, cx2, y + 26, Kind.TOGGLE, m));

            if (m.expanded) {
                text(g, m.description, cx1 + 10, y + 27, FAINT);
                int sy = y + 26 + 14;
                for (Setting s : m.settings) {
                    drawSetting(g, s, cx1, cx2, sy);
                    sy += 24;
                }
            }
            y += h + 6;
            total += h + 6;
        }
        g.disableScissor();
        contentH = total;
    }

    private void drawSetting(GuiGraphics g, Setting s, int cx1, int cx2, int sy) {
        switch (s.type) {
            case BOOL -> {
                s.anim += ((s.bool ? 1f : 0f) - s.anim) * 0.35f;
                text(g, s.name, cx1 + 10, sy + 8, DIM);
                pill(g, cx2 - 36, sy + 6, s.anim);
                hits.add(new Hit(cx1, sy, cx2, sy + 24, Kind.BOOL, s));
            }
            case NUMBER -> {
                text(g, s.name, cx1 + 10, sy + 1, DIM);
                String v = s.step >= 1 ? String.valueOf(s.asInt())
                        : String.format(s.step >= 0.1 ? "%.1f" : "%.2f", s.value);
                text(g, v, cx2 - 10 - font.width(v), sy + 1, WHITE);
                int tx1 = cx1 + 10, tx2 = cx2 - 10, ty2 = sy + 15;
                float t = (float) ((s.value - s.min) / (s.max - s.min));
                Gfx.rr(g, tx1, ty2, tx2, ty2 + 4, 2, 0x33FFFFFF);
                int fx = tx1 + (int) ((tx2 - tx1) * t);
                if (fx - tx1 > 1) Gfx.rr(g, tx1, ty2, fx, ty2 + 4, 2, 0xCCFFFFFF);
                Gfx.rr(g, fx - 3, ty2 - 3, fx + 3, ty2 + 7, 3, 0xF5FFFFFF);
                hits.add(new Hit(tx1, sy + 10, tx2, sy + 23, Kind.SLIDER, s));
            }
            case KEY -> {
                boolean wait = listening == s;
                text(g, s.name, cx1 + 10, sy + 8, DIM);
                box(g, cx2 - 90, sy + 4, cx2 - 10, sy + 20, wait ? "press a key..." : Keys.name(s.key), wait);
                hits.add(new Hit(cx2 - 90, sy + 4, cx2 - 10, sy + 20, Kind.KEY, s));
            }
            case MODE -> {
                text(g, s.name, cx1 + 10, sy + 8, DIM);
                box(g, cx2 - 90, sy + 4, cx2 - 10, sy + 20, s.modes[s.mode], false);
                hits.add(new Hit(cx2 - 90, sy + 4, cx2 - 10, sy + 20, Kind.MODE, s));
            }
        }
    }

    private void box(GuiGraphics g, int x1, int y1, int x2, int y2, String label, boolean active) {
        Gfx.rr(g, x1, y1, x2, y2, 6, active ? 0x55FFFFFF : 0x26FFFFFF);
        Gfx.border(g, x1, y1, x2, y2, 6, 0x35FFFFFF);
        text(g, label, x1 + (x2 - x1 - font.width(label)) / 2, y1 + 4, WHITE);
    }

    private void pill(GuiGraphics g, int x, int y, float a) {
        Gfx.rr(g, x, y, x + 24, y + 12, 6, Gfx.lerp(0x40FFFFFF, 0xB034C759, a));
        Gfx.border(g, x, y, x + 24, y + 12, 6, 0x30FFFFFF);
        int kx = x + 1 + Math.round(12 * a);
        Gfx.rr(g, kx, y + 1, kx + 10, y + 11, 5, 0xF5FFFFFF);
    }

    private void text(GuiGraphics g, String s, int x, int y, int color) {
        g.drawString(font, s, x, y, color, false);
    }

    private void setSlider(Hit h, double mx) {
        Setting s = (Setting) h.ref();
        double t = Math.max(0, Math.min(1, (mx - h.x1()) / (double) (h.x2() - h.x1())));
        s.set(s.min + t * (s.max - s.min));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        double mx = e.x(), my = e.y();
        int b = e.button();
        listening = null;
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!h.in(mx, my)) continue;
            if (h.kind() != Kind.TAB && (my < cy1 || my >= cy2)) continue;
            switch (h.kind()) {
                case TAB -> { category = (Category) h.ref(); scroll = 0; }
                case ARROW -> ((Module) h.ref()).expanded ^= true;
                case TOGGLE -> {
                    Module m = (Module) h.ref();
                    if (b == 1) m.expanded ^= true; else m.toggle();
                }
                case BOOL -> ((Setting) h.ref()).bool ^= true;
                case SLIDER -> { dragging = h; setSlider(h, mx); }
                case KEY -> listening = (Setting) h.ref();
                case MODE -> ((Setting) h.ref()).cycle();
            }
            return true;
        }
        return super.mouseClicked(e, dbl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (dragging != null) { setSlider(dragging, e.x()); return true; }
        return super.mouseDragged(e, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent e) {
        dragging = null;
        return super.mouseReleased(e);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scroll -= (float) sy * 16f;
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent e) {
        if (listening != null) {
            int k = e.key();
            if (k == GLFW.GLFW_KEY_BACKSPACE || k == GLFW.GLFW_KEY_DELETE) listening.key = -1;
            else if (k != GLFW.GLFW_KEY_ESCAPE) listening.key = k;
            listening = null;
            return true;
        }
        if (e.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) { onClose(); return true; }
        return super.keyPressed(e);
    }
}
