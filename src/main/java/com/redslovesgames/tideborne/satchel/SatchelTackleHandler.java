package com.redslovesgames.tideborne.satchel;

import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;

/** Server-owned physical tackle preset handler. */
public final class SatchelTackleHandler extends ScreenHandler {
    public static final int NEW = 100;
    public static final int DELETE = 101;
    public static final int EQUIP = 102;

    private final PlayerInventory playerInventory;
    private final ItemStack satchel;
    private final Hand hand;
    private final SimpleInventory pockets = new SimpleInventory(SatchelPreset.SLOTS.size());
    private int selectedPreset;
    private List<SatchelPreset> decodedPresets;

    public SatchelTackleHandler(int syncId, PlayerInventory playerInventory, ItemStack satchel, Hand hand) {
        super(null, syncId);
        this.playerInventory = playerInventory;
        this.satchel = satchel;
        this.hand = hand;
        this.decodedPresets = AnglersSatchelStorage.presets(satchel, playerInventory.player.getRegistryManager());
        loadSelected();
        for (int i = 0; i < SatchelPreset.SLOTS.size(); i++) {
            final int slotIndex = i;
            addSlot(new Slot(pockets, i, 8 + i * 18, 18) {
                @Override public boolean canInsert(ItemStack stack) {
                    return FishingGearRegistry.accepts(SatchelPreset.SLOTS.get(slotIndex), stack);
                }
                @Override public void markDirty() {
                    super.markDirty();
                    saveSelected();
                }
            });
        }
        // Preserve the twelve-slot tackle/equipment layout used by the physical-preset protocol.
        // These six projection slots are server read-only; the first player-inventory slot remains 12.
        SimpleInventory equipmentPreview = new SimpleInventory(SatchelPreset.SLOTS.size());
        for (int i = 0; i < SatchelPreset.SLOTS.size(); i++) {
            addSlot(new Slot(equipmentPreview, i, 8 + i * 18, 36) {
                @Override public boolean canInsert(ItemStack stack) {
                    return false;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int index = column + row * 9 + 9;
                addSlot(new Slot(playerInventory, index, 8 + column * 18, 68 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 126));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return attached(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        if (!attached(player) || slot < 0 || slot >= this.slots.size()) return ItemStack.EMPTY;
        Slot source = this.slots.get(slot);
        if (!source.hasStack()) return ItemStack.EMPTY;
        ItemStack moving = source.getStack();
        ItemStack original = moving.copy();
        int tackleSlots = SatchelPreset.SLOTS.size();
        int playerStart = tackleSlots * 2;
        if (slot < tackleSlots) {
            if (!insertItem(moving, playerStart, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (slot >= playerStart) {
            int target = -1;
            for (int i = 0; i < tackleSlots; i++) {
                if (!this.slots.get(i).hasStack() && FishingGearRegistry.accepts(SatchelPreset.SLOTS.get(i), moving)) {
                    target = i;
                    break;
                }
            }
            if (target < 0 || !insertItem(moving, target, target + 1, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (moving.isEmpty()) source.setStack(ItemStack.EMPTY);
        else source.markDirty();
        saveSelected();
        return original;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (!attached(player)) return false;
        if (id == NEW) {
            ArrayList<SatchelPreset> presets = new ArrayList<>(AnglersSatchelStorage.presets(satchel, player.getRegistryManager()));
            presets.add(SatchelPreset.empty("Custom Preset"));
            AnglersSatchelStorage.setPresets(satchel, presets, player.getRegistryManager());
            selectedPreset = presets.size() - 1;
            refresh();
            return true;
        }
        if (id == DELETE) {
            ArrayList<SatchelPreset> presets = new ArrayList<>(AnglersSatchelStorage.presets(satchel, player.getRegistryManager()));
            if (selectedPreset < 0 || selectedPreset >= presets.size()) return false;
            if (presets.get(selectedPreset).items().stream().anyMatch(stack -> !stack.isEmpty())) return false;
            presets.remove(selectedPreset);
            if (presets.isEmpty()) presets.add(SatchelPreset.empty("Custom Preset"));
            AnglersSatchelStorage.setPresets(satchel, presets, player.getRegistryManager());
            selectedPreset = Math.min(selectedPreset, presets.size() - 1);
            refresh();
            return true;
        }
        if (id == EQUIP) {
            saveSelected();
            List<SatchelPreset> presets = AnglersSatchelStorage.presets(satchel, player.getRegistryManager());
            if (selectedPreset < 0 || selectedPreset >= presets.size()) return false;
            ItemStack held = player.getMainHandStack();
            SatchelTackleExchange.Plan plan = SatchelTackleExchange.plan(held, presets.get(selectedPreset).items());
            if (!plan.success()) return false;
            player.setStackInHand(Hand.MAIN_HAND, plan.rod());
            ArrayList<SatchelPreset> updated = new ArrayList<>(presets);
            updated.set(selectedPreset, presets.get(selectedPreset).withItems(plan.pockets()));
            AnglersSatchelStorage.setPresets(satchel, updated, player.getRegistryManager());
            refresh();
            return true;
        }
        if (id >= 0) {
            List<SatchelPreset> presets = AnglersSatchelStorage.presets(satchel, player.getRegistryManager());
            if (id < presets.size()) {
                selectedPreset = id;
                refresh();
                return true;
            }
        }
        return false;
    }

    public void rename(PlayerEntity player, UUID presetId, String name) {
        if (!attached(player) || presetId == null) return;
        ArrayList<SatchelPreset> presets = new ArrayList<>(AnglersSatchelStorage.presets(satchel, player.getRegistryManager()));
        for (int i = 0; i < presets.size(); i++) {
            if (presets.get(i).id().equals(presetId)) {
                presets.set(i, presets.get(i).renamed(name == null || name.isBlank() ? presets.get(i).name() : name.trim()));
                AnglersSatchelStorage.setPresets(satchel, presets, player.getRegistryManager());
                refresh();
                return;
            }
        }
    }

    @Override
    public void sendContentUpdates() {
        refreshDecodedOnly();
        super.sendContentUpdates();
    }

    private boolean attached(PlayerEntity player) {
        return player != null && player.getStackInHand(hand) == satchel;
    }

    private void refresh() {
        refreshDecodedOnly();
        loadSelected();
        sendContentUpdates();
    }

    private void refreshDecodedOnly() {
        List<SatchelPreset> latest = AnglersSatchelStorage.presets(satchel, playerInventory.player.getRegistryManager());
        if (!samePresets(decodedPresets, latest)) decodedPresets = latest;
    }

    private void loadSelected() {
        if (decodedPresets == null || decodedPresets.isEmpty()) {
            for (int i = 0; i < pockets.size(); i++) pockets.setStack(i, ItemStack.EMPTY);
            return;
        }
        selectedPreset = Math.max(0, Math.min(selectedPreset, decodedPresets.size() - 1));
        List<ItemStack> items = decodedPresets.get(selectedPreset).items();
        for (int i = 0; i < pockets.size(); i++) pockets.setStack(i, items.get(i).copy());
    }

    private void saveSelected() {
        if (decodedPresets == null || decodedPresets.isEmpty() || selectedPreset < 0 || selectedPreset >= decodedPresets.size()) return;
        ArrayList<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < pockets.size(); i++) items.add(pockets.getStack(i).copy());
        ArrayList<SatchelPreset> presets = new ArrayList<>(decodedPresets);
        presets.set(selectedPreset, presets.get(selectedPreset).withItems(items));
        AnglersSatchelStorage.setPresets(satchel, presets, playerInventory.player.getRegistryManager());
        decodedPresets = List.copyOf(presets);
    }

    private static boolean samePresets(List<SatchelPreset> a, List<SatchelPreset> b) {
        if (a == b) return true;
        if (a == null || b == null || a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            SatchelPreset x = a.get(i), y = b.get(i);
            if (!x.id().equals(y.id()) || !x.name().equals(y.name()) || x.items().size() != y.items().size()) return false;
            for (int j = 0; j < x.items().size(); j++) if (!ItemStack.areEqual(x.items().get(j), y.items().get(j))) return false;
        }
        return true;
    }
}
