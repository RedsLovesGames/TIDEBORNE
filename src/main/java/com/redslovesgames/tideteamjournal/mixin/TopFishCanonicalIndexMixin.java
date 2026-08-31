package com.redslovesgames.tideteamjournal.mixin;

import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenRecordIndexer;
import com.redslovesgames.tideteamjournal.TeamCanonicalJournalCapture;
import com.redslovesgames.tideteamjournal.TeamProgressStore;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces the reconstructed Top 12 insertion with canonical deterministic Top 15 indexing. */
@Mixin(value = TeamProgressStore.class, remap = false)
abstract class TopFishCanonicalIndexMixin {
    @Shadow @Final private static ThreadLocal TIDEBORNE_CURRENT_FISH;
    @Shadow @Final private static ThreadLocal TIDEBORNE_LAST_FISH;

    @Inject(method = "tideborneRecordCurrentTopFish", at = @At("HEAD"), cancellable = true)
    private static void tideborne$indexCanonicalTopFish(
            NbtCompound root,
            UUID catcherId,
            String catcherName,
            CallbackInfo callback
    ) {
        SpecimenData specimen = TeamCanonicalJournalCapture.currentSpecimen().orElse(null);
        if (specimen != null && specimen.fishScore().isPresent()) {
            NbtCompound candidate = CanonicalSpecimenRecordIndexer.project(specimen);
            NbtCompound compatibility = currentCompatibilityTag();
            if (compatibility != null) {
                candidate.putInt("fish_stars", compatibility.getInt("fish_stars"));
            }
            candidate.putUuid("catcher_id", catcherId);
            candidate.putString("catcher_name", catcherName == null ? "" : catcherName);
            candidate.putLong("timestamp", System.currentTimeMillis());
            CanonicalSpecimenRecordIndexer.indexTeamTopFish(root, candidate);
        }
        callback.cancel();
    }

    private static NbtCompound currentCompatibilityTag() {
        Object current = TIDEBORNE_CURRENT_FISH.get();
        if (current instanceof NbtCompound tag) {
            return tag;
        }
        Object previous = TIDEBORNE_LAST_FISH.get();
        return previous instanceof NbtCompound tag ? tag : null;
    }
}
