/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.satchel;

import java.util.Objects;

public final class SatchelProtectionPolicy {
   private SatchelProtectionPolicy() {
   }

   public static boolean shouldProtect(SatchelState state, SatchelProtectionPolicy.Evidence evidence) {
      Objects.requireNonNull(state, "state");
      Objects.requireNonNull(evidence, "evidence");
      return state.isFeatureUnlocked(SatchelFeature.TROPHY_LOCK) && state.isFeatureEnabled(SatchelFeature.TROPHY_LOCK)
         ? state.protectionRuleEnabled(SatchelProtectionRule.MUTATED.id()) && evidence.mutated()
            || state.protectionRuleEnabled(SatchelProtectionRule.TROPHY_SIZE.id()) && evidence.percentile() >= 85.0 && evidence.percentile() < 95.0
            || state.protectionRuleEnabled(SatchelProtectionRule.LEGENDARY_SIZE.id()) && evidence.percentile() >= 95.0 && evidence.percentile() <= 100.0
            || state.protectionRuleEnabled(SatchelProtectionRule.PERSONAL_LARGEST.id()) && evidence.personalLargest()
            || state.protectionRuleEnabled(SatchelProtectionRule.PERSONAL_SMALLEST.id()) && evidence.personalSmallest()
            || state.protectionRuleEnabled(SatchelProtectionRule.TIDE_LEGENDARY_RARITY.id()) && evidence.tideLegendaryRarity()
         : false;
   }

   public record Evidence(boolean mutated, double percentile, boolean personalLargest, boolean personalSmallest, boolean tideLegendaryRarity) {
   }
}
