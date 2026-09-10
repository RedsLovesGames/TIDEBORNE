package com.redslovesgames.tideborne.journal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.li64.tide.data.player.TidePlayerData;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.junit.jupiter.api.Test;

class MixinReductionArchitectureTest {
   @Test
   void journalSyncNoLongerInterceptsTidePlayerDataPacket() throws IOException {
      try (InputStream stream = getClass().getResourceAsStream("/tideborne.mixins.json")) {
         assertNotNull(stream);
         String config = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
         assertFalse(config.contains("SyncPlayerDataMsgMixin"));
      }
   }

   @Test
   void catchLifecycleIsOwnedBySharedBridge() throws NoSuchMethodException {
      assertNotNull(TeamJournalCatchBridge.class.getDeclaredMethod("beginDirectLog", TidePlayerData.class, ItemStack.class));
      assertNotNull(TeamJournalCatchBridge.class.getDeclaredMethod("finishDirectLog", TidePlayerData.class, ServerPlayerEntity.class));
      assertNotNull(TeamJournalCatchBridge.class.getDeclaredMethod("beginTryLog", ItemStack.class, ServerPlayerEntity.class));
      assertNotNull(TeamJournalCatchBridge.class.getDeclaredMethod("finishTryLog", boolean.class, ServerPlayerEntity.class));
   }
}
