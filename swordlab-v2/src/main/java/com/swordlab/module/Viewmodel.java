package com.swordlab.module;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

public class Viewmodel extends Module {
    private final Setting x = add(Setting.number("X", 0, -1, 1, 0.05));
    private final Setting y = add(Setting.number("Y", 0, -1, 1, 0.05));
    private final Setting z = add(Setting.number("Z", 0, -1, 1, 0.05));
    private final Setting scale = add(Setting.number("Scale", 1, 0.5, 1.5, 0.05));

    public Viewmodel() {
        super("Viewmodel", "Moves and scales your held item/hand", Category.VISUALS);
    }

    @Override
    public void onTick(Minecraft mc) {}

    public void apply(PoseStack ps) {
        ps.translate(x.value, y.value, z.value);
        float s = (float) scale.value;
        ps.scale(s, s, s);
    }
}
