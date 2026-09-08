package com.redslovesgames.tideboundcompatibility.fishing;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.fishing.v2.FishingGearModifiers;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import java.util.Objects;

/** @deprecated Catch-loss compatibility facade composing leader, bobber and rod protection.
 * Legacy Steel Leader attachment state resolves as the Iron Leader tier.
 */
@Deprecated
public final class SteelLeaderGearModifiers {
    private SteelLeaderGearModifiers() {}
    public static FishingGearModifiers forHook(TideFishingHook hook, TideboundConfig.Values config) {
        return FishingGearModifiers.compose(LeaderGearModifiers.forHook(hook, config), BobberGearModifiers.forHook(hook),
                hook == null ? FishingGearModifiers.neutral() : TideborneFishingGearModifiers.forRod(hook.getRod(), config));
    }
    public static FishingGearModifiers forAttachmentState(boolean attached, TideboundConfig.Values config) {
        Objects.requireNonNull(config, "config");
        return LeaderGearModifiers.forTier(attached ? LeaderTier.IRON : null, config.enableApexCompat);
    }
}
