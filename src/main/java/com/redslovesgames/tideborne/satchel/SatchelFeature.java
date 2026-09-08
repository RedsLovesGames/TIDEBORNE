/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.satchel;

import com.redslovesgames.tideborne.config.TideTraitsConfigManager;
import java.util.Arrays;
import java.util.Optional;

public enum SatchelFeature {
   TACKLE_ORGANIZER("tackle_organizer", 100, false),
   AUTO_STOW("auto_stow", 200, false),
   RECORD_KEEPER("record_keeper", 250, false),
   TRAIT_SCANNER("trait_scanner", 300, false),
   TROPHY_LOCK("trophy_lock", 350, false),
   SHARED_LEDGER("shared_ledger", 400, true);

   private final String id;
   private final int xpCost;
   private final boolean requiresMultiplayerExtras;

   SatchelFeature(String id, int xpCost, boolean requiresMultiplayerExtras) {
      this.id = id;
      this.xpCost = xpCost;
      this.requiresMultiplayerExtras = requiresMultiplayerExtras;
   }

   public String id() {
      return this.id;
   }

   public int xpCost() {
      return TideTraitsConfigManager.current().satchel().featureCost(this.id, this.xpCost);
   }

   public boolean requiresMultiplayerExtras() {
      return this.requiresMultiplayerExtras;
   }

   public static Optional<SatchelFeature> byId(String id) {
      return Arrays.stream(values()).filter(feature -> feature.id.equals(id)).findFirst();
   }
}
