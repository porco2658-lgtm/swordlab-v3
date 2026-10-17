package com.swordlab.module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class SwingAnimation extends Module {
    private final Setting style = add(Setting.mode("Style", 1, "Vanilla", "Smooth", "Chop", "Stab", "Spin"));
    private final Setting duration = add(Setting.number("Duration (ticks)", 6, 2, 20, 1));

    public SwingAnimation() {
        super("Swing Animation", "Changes the first-person swing + speed", Category.VISUALS);
    }

    @Override
    public void onTick(Minecraft mc) {}

    public boolean customStyle() { return style.mode != 0; }
    public int duration() { return duration.asInt(); }

    /** Replaces vanilla's attack transform. Every style is identity at progress 0 and 1. */
    public void apply(PoseStack ps, HumanoidArm arm, float sp) {
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        float s = Mth.sin(sp * (float) Math.PI);
        switch (style.mode) {
            case 1 -> { // Smooth
                ps.translate(side * 0.08f * s, -0.08f * s, -0.15f * s);
                ps.mulPose(Axis.XP.rotationDegrees(-55f * s));
                ps.mulPose(Axis.ZP.rotationDegrees(side * -12f * s));
            }
            case 2 -> { // Chop
                ps.translate(0f, 0.05f * s, -0.1f * s);
                ps.mulPose(Axis.XP.rotationDegrees(-95f * s));
            }
            case 3 -> { // Stab
                ps.translate(0f, 0f, -0.55f * s);
                ps.mulPose(Axis.XP.rotationDegrees(-8f * s));
            }
            case 4 -> ps.mulPose(Axis.ZP.rotationDegrees(side * 360f * sp)); // Spin
            default -> {}
        }
    }
}
