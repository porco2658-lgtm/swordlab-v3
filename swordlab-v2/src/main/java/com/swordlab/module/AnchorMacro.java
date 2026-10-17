package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/**
 * Look at a block, press the activate key: places an anchor, charges it with glowstone, then
 * switches to the totem slot and right-clicks it to detonate. Slots are set with the number keys 1-9.
 */
public class AnchorMacro extends Module {
    private final Setting activate = add(Setting.key("Activate key", GLFW.GLFW_KEY_G));
    private final Setting anchorKey = add(Setting.key("Anchor key", GLFW.GLFW_KEY_1));
    private final Setting glowKey = add(Setting.key("Glowstone key", GLFW.GLFW_KEY_2));
    private final Setting totemKey = add(Setting.key("Totem key", GLFW.GLFW_KEY_3));
    private final Setting delay = add(Setting.number("Step delay (ticks)", 1, 0, 5, 1));

    private enum S { IDLE, PLACE, CHARGE, DETONATE }

    private S state = S.IDLE;
    private int wait;
    private boolean wasDown;
    private BlockHitResult placeHit;
    private BlockPos anchorPos;

    public AnchorMacro() {
        super("Anchor Macro", "Place, charge and detonate an anchor", Category.CRYSTAL);
    }

    @Override
    public void onDisable() { state = S.IDLE; }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || mc.gameMode == null) { state = S.IDLE; return; }

        boolean down = mc.screen == null && Keys.down(activate.key);
        boolean pressed = down && !wasDown;
        wasDown = down;

        if (state == S.IDLE) {
            if (pressed) start(mc);
            return;
        }
        if (wait > 0) { wait--; return; }

        switch (state) {
            case PLACE -> {
                if (!use(mc, p, anchorKey.key, placeHit)) return;
                state = S.CHARGE;
                wait = delay.asInt();
            }
            case CHARGE -> {
                if (!mc.level.getBlockState(anchorPos).is(Blocks.RESPAWN_ANCHOR)) { state = S.IDLE; return; }
                if (!use(mc, p, glowKey.key, anchorHit())) return;
                state = S.DETONATE;
                wait = delay.asInt();
            }
            case DETONATE -> {
                use(mc, p, totemKey.key, anchorHit());
                state = S.IDLE;
            }
            default -> state = S.IDLE;
        }
    }

    private void start(Minecraft mc) {
        if (!(mc.hitResult instanceof BlockHitResult bh) || bh.getType() != HitResult.Type.BLOCK) return;
        if (mc.level.getBlockState(bh.getBlockPos()).is(Blocks.RESPAWN_ANCHOR)) {
            anchorPos = bh.getBlockPos();
            state = S.CHARGE;
        } else {
            placeHit = bh;
            anchorPos = bh.getBlockPos().relative(bh.getDirection());
            state = S.PLACE;
        }
        wait = 0;
    }

    private BlockHitResult anchorHit() {
        return new BlockHitResult(Vec3.atCenterOf(anchorPos), Direction.UP, anchorPos, false);
    }

    /** Selects the hotbar slot bound to the key and right-clicks the block. */
    private boolean use(Minecraft mc, LocalPlayer p, int key, BlockHitResult hit) {
        int slot = Keys.slot(key);
        if (slot < 0) { state = S.IDLE; return false; }
        Inv.select(p, slot);
        mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
        p.swing(InteractionHand.MAIN_HAND);
        return true;
    }
}
