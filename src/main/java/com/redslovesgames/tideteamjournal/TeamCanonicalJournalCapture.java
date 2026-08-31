package com.redslovesgames.tideteamjournal;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.player.FishStats;
import com.li64.tide.data.player.TidePlayerData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tideborne.fishing.v2.integration.JournalSpecimenStore;
import java.util.Optional;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;

/** Server-thread catch context used only to persist canonical team journal specimen snapshots. */
public final class TeamCanonicalJournalCapture {
    private static final ThreadLocal<ItemStack> CURRENT_CATCH = new ThreadLocal<>();
    private static final ThreadLocal<ItemStack> LAST_CATCH = new ThreadLocal<>();

    private TeamCanonicalJournalCapture() {
    }

    public static void begin(ItemStack stack) {
        CURRENT_CATCH.remove();
        LAST_CATCH.remove();
        if (stack != null && !stack.isEmpty() && CanonicalSpecimenStorage.read(stack).isPresent()) {
            CURRENT_CATCH.set(stack.copy());
        }
    }

    /**
     * Mirrors TeamProgressStore's two-stage current/last lifecycle. The first nested Tide clear moves
     * the catch to LAST_CATCH so post-save record indexing can still consume it. A later clear with no
     * active current catch removes the retained value and prevents cross-catch leakage.
     */
    public static void clear() {
        ItemStack current = CURRENT_CATCH.get();
        if (current != null && !current.isEmpty()) {
            LAST_CATCH.set(current);
            CURRENT_CATCH.remove();
        } else {
            LAST_CATCH.remove();
        }
    }

    /** Returns the finalized server-owned catch specimen without exposing a mutable stack. */
    public static Optional<SpecimenData> currentSpecimen() {
        ItemStack stack = CURRENT_CATCH.get();
        if (stack == null || stack.isEmpty()) {
            stack = LAST_CATCH.get();
        }
        return stack == null || stack.isEmpty() ? Optional.empty() : CanonicalSpecimenStorage.read(stack);
    }

    public static void capture(NbtCompound teamRoot, TidePlayerData before) {
        ItemStack stack = CURRENT_CATCH.get();
        if (stack == null || stack.isEmpty()) {
            stack = LAST_CATCH.get();
        }
        if (stack == null || stack.isEmpty() || teamRoot == null || before == null) {
            return;
        }

        SpecimenData specimen = CanonicalSpecimenStorage.read(stack).orElse(null);
        FishData fish = FishData.get(stack).orElse(null);
        if (specimen == null || fish == null) {
            return;
        }

        RegistryEntry<Item> fishEntry = fish.fish();
        Optional<FishStats> previous = before.getDataFor(fishEntry.value())
                .flatMap(data -> data.stats)
                .filter(stats -> !stats.isEmpty());
        double length = specimen.finalLength();
        boolean largest = previous.isEmpty() || length > previous.orElseThrow().getLargestCatch() + tolerance(previous.orElseThrow().getLargestCatch());
        boolean smallest = previous.isEmpty() || length < previous.orElseThrow().getSmallestCatch() - tolerance(previous.orElseThrow().getSmallestCatch());

        JournalSpecimenStore.capture(teamRoot, specimen, largest, smallest);
    }

    private static double tolerance(double value) {
        return Double.isFinite(value) ? Math.max(1.0E-6, Math.ulp(value) * 4.0) : 0.0;
    }
}
