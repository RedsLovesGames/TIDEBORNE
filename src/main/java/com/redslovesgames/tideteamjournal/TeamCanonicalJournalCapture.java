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
    private static final ThreadLocal<CapturedCatch> CURRENT_CATCH = new ThreadLocal<>();
    private static final ThreadLocal<CapturedCatch> LAST_CATCH = new ThreadLocal<>();

    private TeamCanonicalJournalCapture() {
    }

    public static void begin(ItemStack stack) {
        CURRENT_CATCH.remove();
        LAST_CATCH.remove();
        if (stack != null && !stack.isEmpty()) {
            CanonicalSpecimenStorage.read(stack).ifPresent(specimen ->
                    CURRENT_CATCH.set(new CapturedCatch(stack.copy(), specimen)));
        }
    }

    /**
     * Mirrors TeamProgressStore's two-stage current/last lifecycle. The first nested Tide clear moves
     * the catch to LAST_CATCH so post-save record indexing can still consume it. A later clear with no
     * active current catch removes the retained value and prevents cross-catch leakage.
     */
    public static void clear() {
        CapturedCatch current = CURRENT_CATCH.get();
        if (current != null) {
            LAST_CATCH.set(current);
            CURRENT_CATCH.remove();
        } else {
            LAST_CATCH.remove();
        }
    }

    /** Returns the finalized server-owned catch specimen without exposing a mutable stack. */
    public static Optional<SpecimenData> currentSpecimen() {
        CapturedCatch current = currentCatch();
        return current == null ? Optional.empty() : Optional.of(current.specimen());
    }

    private static CapturedCatch currentCatch() {
        CapturedCatch current = CURRENT_CATCH.get();
        return current == null ? LAST_CATCH.get() : current;
    }

    private record CapturedCatch(ItemStack stack, SpecimenData specimen) {}

    public static void capture(NbtCompound teamRoot, TidePlayerData before) {
        CapturedCatch current = currentCatch();
        if (current == null || teamRoot == null || before == null) {
            return;
        }

        SpecimenData specimen = current.specimen();
        FishData fish = FishData.get(current.stack()).orElse(null);
        if (specimen == null || fish == null) {
            return;
        }

        RegistryEntry<Item> fishEntry = fish.fish();
        Optional<FishStats> previous = before.getDataFor(fishEntry.value())
                .flatMap(data -> data.stats)
                .filter(stats -> !stats.isEmpty());
        double length = specimen.finalLength();
        boolean largest = RecordHolderStore.isLargerPhysicalRecord(length, previous.isPresent(),
                previous.map(FishStats::getLargestCatch).orElse(0.0));
        boolean smallest = RecordHolderStore.isSmallerPhysicalRecord(length, previous.isPresent(),
                previous.map(FishStats::getSmallestCatch).orElse(0.0));

        JournalSpecimenStore.capture(teamRoot, specimen, largest, smallest);
    }

}
