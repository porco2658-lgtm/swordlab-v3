package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;

public final class Inv {
    private Inv() {}

    public static void select(LocalPlayer p, int hotbar) {
        if (hotbar >= 0 && hotbar < 9) p.getInventory().setSelectedSlot(hotbar);
    }

    /** Inventory index (0-8 hotbar, 9-35 main) of the first matching item in [from, to], or -1. */
    public static int find(LocalPlayer p, Item item, int from, int to) {
        for (int i = from; i <= to; i++) if (p.getInventory().getItem(i).is(item)) return i;
        return -1;
    }

    /** Inventory index -> slot id inside the player inventory menu. */
    public static int menuSlot(int invIndex) { return invIndex < 9 ? 36 + invIndex : invIndex; }

    /** SWAP click: button 0-8 = hotbar slot, 40 = offhand. */
    public static void swap(Minecraft mc, LocalPlayer p, int invIndex, int button) {
        mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId, menuSlot(invIndex), button, ClickType.SWAP, p);
    }
}
