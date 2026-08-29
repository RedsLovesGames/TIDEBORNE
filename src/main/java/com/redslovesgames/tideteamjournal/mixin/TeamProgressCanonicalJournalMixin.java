package com.redslovesgames.tideteamjournal.mixin;

import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tideteamjournal.StoredFishScoreStorage;
import com.redslovesgames.tideteamjournal.TeamCanonicalJournalCapture;
import com.redslovesgames.tideteamjournal.TeamProgressStore;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps team journal score/catch capture on the finalized server canonical specimen. */
@Mixin(value = TeamProgressStore.class, remap = false)
abstract class TeamProgressCanonicalJournalMixin {
    /**
     * Shared FishScore consumer boundary used by Satchel/profile/item display code.
     * Canonical storage may migrate a readable legacy specimen once; an unreadable or scoreless
     * specimen stays scoreless instead of falling through to the reconstructed pre-V2 formula.
     */
    @Inject(method = "tideborneFishScore", at = @At("HEAD"), cancellable = true)
    private static void tideborne$canonicalScore(ItemStack stack, CallbackInfoReturnable<Double> callback) {
        SpecimenData specimen = CanonicalSpecimenStorage.read(stack).orElse(null);
        callback.setReturnValue(specimen != null && specimen.fishScore().isPresent()
                ? (double) specimen.fishScore().getAsInt()
                : -1.0);
    }

    /** Leaderboard capture uses the same canonical specimen score boundary as other consumers. */
    @Redirect(
            method = "tideborneBeginCatch",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/redslovesgames/tideteamjournal/TeamProgressStore;tideborneFishScore(Lnet/minecraft/item/ItemStack;)D"
            )
    )
    private static double tideborne$storedLeaderboardScore(ItemStack stack) {
        SpecimenData specimen = CanonicalSpecimenStorage.read(stack).orElse(null);
        return specimen != null && specimen.fishScore().isPresent()
                ? specimen.fishScore().getAsInt()
                : -1.0;
    }

    @Inject(method = "ensureInitialized", at = @At("RETURN"), cancellable = true)
    private static void tideborne$migrateStoredScores(NbtCompound root, CallbackInfoReturnable<Boolean> callback) {
        if (StoredFishScoreStorage.migrateRoot(root) && !Boolean.TRUE.equals(callback.getReturnValue())) {
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "mergeTrackedDataOnce", at = @At("HEAD"))
    private static void tideborne$prepareStoredScoreMerge(
            NbtCompound targetRoot,
            NbtCompound sourceRoot,
            int historyLimit,
            CallbackInfo callback
    ) {
        StoredFishScoreStorage.migrateRoot(targetRoot);
        StoredFishScoreStorage.migrateRoot(sourceRoot);
    }

    @Inject(method = "mergeTrackedDataOnce", at = @At("RETURN"))
    private static void tideborne$finishStoredScoreMerge(
            NbtCompound targetRoot,
            NbtCompound sourceRoot,
            int historyLimit,
            CallbackInfo callback
    ) {
        StoredFishScoreStorage.acceptCompatibilityWrites(targetRoot);
    }

    @Inject(method = "readHistory", at = @At("HEAD"))
    private static void tideborne$migrateHistoryScores(NbtCompound root, CallbackInfoReturnable<?> callback) {
        StoredFishScoreStorage.migrateRoot(root);
    }

    @Inject(method = "writeHistory", at = @At("RETURN"))
    private static void tideborne$persistHistoryScores(NbtCompound root, java.util.List<?> events, int limit, CallbackInfo callback) {
        StoredFishScoreStorage.acceptCompatibilityWrites(root);
    }

    @Inject(method = "tideborneRegisterContributorFishScore", at = @At("HEAD"))
    private static void tideborne$canonicalizeContributorRead(java.util.UUID id, NbtCompound tag, CallbackInfo callback) {
        StoredFishScoreStorage.migrateLegacyScore(tag);
        StoredFishScoreStorage.syncCompatibilityMirror(tag);
    }

    @Inject(method = "tideborneUpdateContributorFishScore", at = @At("HEAD"))
    private static void tideborne$prepareContributorWrite(NbtCompound tag, CallbackInfo callback) {
        StoredFishScoreStorage.migrateLegacyScore(tag);
        StoredFishScoreStorage.syncCompatibilityMirror(tag);
    }

    @Inject(method = "tideborneUpdateContributorFishScore", at = @At("RETURN"))
    private static void tideborne$persistContributorWrite(NbtCompound tag, CallbackInfo callback) {
        StoredFishScoreStorage.acceptCompatibilityWrite(tag);
        StoredFishScoreStorage.syncCompatibilityMirror(tag);
    }

    @Inject(method = "tideborneRegisterEventMeta", at = @At("HEAD"))
    private static void tideborne$canonicalizeEventRead(TeamProgressStore.RecordEvent event, NbtCompound tag, CallbackInfo callback) {
        StoredFishScoreStorage.migrateLegacyScore(tag);
        StoredFishScoreStorage.syncCompatibilityMirror(tag);
    }

    @Inject(method = "tideborneRecordCurrentTopFish", at = @At("HEAD"))
    private static void tideborne$prepareTopFishOrdering(NbtCompound root, java.util.UUID id, String name, CallbackInfo callback) {
        StoredFishScoreStorage.migrateRoot(root);
    }

    @Inject(method = "tideborneRecordCurrentTopFish", at = @At("RETURN"))
    private static void tideborne$persistTopFishScores(NbtCompound root, java.util.UUID id, String name, CallbackInfo callback) {
        StoredFishScoreStorage.acceptCompatibilityWrites(root);
    }

    @Inject(method = "tideborneBeginCatch", at = @At("HEAD"))
    private static void tideborne$beginCanonicalJournalCatch(ItemStack stack, CallbackInfo callback) {
        TeamCanonicalJournalCapture.begin(stack);
    }

    @Inject(method = "tideborneClearCatch", at = @At("TAIL"))
    private static void tideborne$clearCanonicalJournalCatch(CallbackInfo callback) {
        TeamCanonicalJournalCapture.clear();
    }
}
