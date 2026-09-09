package com.redslovesgames.tideborne.journal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BobberConfigMigrationTest {
    @Test void migrateKnownHeartDefaultsButPreserveCustomSettings() {
        for(var old:new BobberBonuses.Bonus[]{new BobberBonuses.Bonus(3,0),new BobberBonuses.Bonus(1,2),new BobberBonuses.Bonus(9,7)}) {
            var config=new ServerConfig.Values();config.bobberBonuses.put("tide:heart_bobber",old);
            assertEquals(old.luck()==9?old:BobberBonuses.Bonus.NONE,ServerConfig.validate(config).bobberBonuses.get("tide:heart_bobber"));
        }
    }
    @Test void displaySynchronizationUsesTheConfiguredValues() {
        var id=net.minecraft.util.Identifier.of("thirdparty","bobber");
        BobberBonuses.updateClient(true,new BobberBonuses.Bonus(1,3),java.util.Map.of(id,new BobberBonuses.Bonus(4,2)));
        assertEquals(new BobberBonuses.Bonus(4,2),BobberBonuses.forClientId(id));
        assertEquals(new BobberBonuses.Bonus(1,3),BobberBonuses.forClientId(net.minecraft.util.Identifier.of("thirdparty","unknown")));
        BobberBonuses.updateClient(false,BobberBonuses.Bonus.NONE,java.util.Map.of());
        assertEquals(BobberBonuses.Bonus.NONE,BobberBonuses.forClientId(id));
    }
}
