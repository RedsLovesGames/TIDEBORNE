package com.redslovesgames.tideborne.fishing.specimen;

import com.li64.tide.data.fishing.SizeData;
import java.util.Map;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FishPercentileServiceTest {
    @Test
    void lazySamplingMatchesWarmupAndReloadInvalidatesOnlyRetainedWork() {
        Identifier id = Identifier.of("minecraft", "cod");
        SizeData size = new SizeData(12, 45, 90);
        FishPercentileService eager = new FishPercentileService();
        FishPercentileService lazy = new FishPercentileService();
        eager.rebuild(Map.of(id, size));
        assertEquals(0, lazy.cachedSpeciesCount());
        for (double length : new double[]{0, 12, 25, 45, 90, 200}) {
            assertEquals(eager.percentile(id, size, length), lazy.percentile(id, size, length));
        }
        assertEquals(1, lazy.cachedSpeciesCount());
        lazy.clear();
        assertEquals(0, lazy.cachedSpeciesCount());
        assertEquals(eager.percentile(id, size, 25), lazy.percentile(id, size, 25));
        SizeData replacement = new SizeData(100, 200, 300);
        eager.rebuild(Map.of(id, replacement));
        assertEquals(eager.percentile(id, replacement, 125), lazy.percentile(id, replacement, 125));
        assertEquals(1, lazy.cachedSpeciesCount());
    }
}
