package com.swordlab.module;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.ItemTags;

public abstract class Module {
    public final String name, description;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();
    public boolean enabled, expanded;
    public float anim; // GUI animation state

    protected Setting requireSword; // only exists on Sword modules

    protected Module(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
        if (category == Category.SWORD) requireSword = add(Setting.bool("Require sword", true));
    }

    protected Setting add(Setting s) { settings.add(s); return s; }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public abstract void onTick(Minecraft mc);

    protected boolean holdingSword(Minecraft mc) {
        return requireSword == null || !requireSword.bool || mc.player.getMainHandItem().is(ItemTags.SWORDS);
    }
}
