package com.redslovesgames.tideborne.journal;

import com.li64.tide.data.player.TidePlayerData;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenGenerator;
import com.redslovesgames.tideborne.journal.JournalSpecimenStore;
import com.redslovesgames.tideborne.journal.JournalSpecimenNetworkCodec;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecordHolderProjectionTest {
    @Test
    void payloadIncludesCanonicalSidecarsWithoutMutatingJournal() {
        NbtCompound root = new NbtCompound();
        SpecimenData specimen = new SpecimenData("minecraft:cod", SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION, 12L, 50, 20, 20, 50,
                SpecimenData.BodyType.GIANT, SpecimenData.Condition.SCARRED,
                SpecimenData.Pigmentation.IRIDESCENT, SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
                true, OptionalDouble.of(300), OptionalInt.of(900), SpecimenData.Provenance.generated());
        JournalSpecimenStore.capture(root, specimen, true, true);
        NbtCompound journal = new NbtCompound();
        journal.putString("opaque", "preserve");
        NbtCompound before = journal.copy();
        NbtCompound packet = RecordHolderStore.attachForClient(journal, root);
        assertEquals(JournalSpecimenNetworkCodec.buildDisplayData(root),
                packet.getCompound(JournalSpecimenNetworkCodec.CLIENT_KEY));
        assertTrue(packet.contains(RecordHolderStore.CLIENT_KEY, 10));
        assertEquals(before, journal);
        packet.putString("opaque", "client");
        assertEquals(before, journal);
    }

    @Test
    void absentCatchDoesNotChangeHolderResult() {
        TeamCanonicalJournalCapture.clear();
        TeamCanonicalJournalCapture.clear();
        assertFalse(RecordHolderStore.updateAfterCatch(new NbtCompound(), new TidePlayerData(),
                new TidePlayerData(), UUID.randomUUID(), "angler"));
    }
}
