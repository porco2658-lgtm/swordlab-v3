package com.swordlab.module;

public class Setting {
    public enum Type { BOOL, NUMBER, KEY, MODE }

    public final String name;
    public final Type type;
    public boolean bool;
    public double value, min, max, step = 1;
    public int key = -1;                 // GLFW key code, -1 = unbound
    public String[] modes = new String[0];
    public int mode;
    public float anim;                   // GUI animation state

    private Setting(String name, Type type) { this.name = name; this.type = type; }

    public static Setting bool(String name, boolean def) {
        Setting s = new Setting(name, Type.BOOL);
        s.bool = def;
        s.anim = def ? 1f : 0f;
        return s;
    }

    public static Setting number(String name, double def, double min, double max, double step) {
        Setting s = new Setting(name, Type.NUMBER);
        s.min = min; s.max = max; s.step = step; s.value = def;
        return s;
    }

    public static Setting key(String name, int defKey) {
        Setting s = new Setting(name, Type.KEY);
        s.key = defKey;
        return s;
    }

    public static Setting mode(String name, int def, String... modes) {
        Setting s = new Setting(name, Type.MODE);
        s.modes = modes;
        s.mode = def;
        return s;
    }

    public void cycle() { if (modes.length > 0) mode = (mode + 1) % modes.length; }

    public void set(double v) {
        v = Math.round(v / step) * step;
        value = Math.max(min, Math.min(max, v));
    }

    public int asInt() { return (int) Math.round(value); }
}
