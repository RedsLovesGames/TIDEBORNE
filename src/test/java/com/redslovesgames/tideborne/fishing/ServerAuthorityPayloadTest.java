package com.redslovesgames.tideborne.fishing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.network.TideboundSettingsUpdatePayload;
import com.redslovesgames.tideborne.journal.network.TeamDataRequestPayload;
import com.redslovesgames.tideborne.discovery.multiplayer.SharedDiscoveryRequestPayload;
import com.redslovesgames.tideborne.satchel.network.SatchelRequestPayload;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ServerAuthorityPayloadTest {
    private static final List<Class<?>> CLIENT_TO_SERVER_PAYLOADS = List.of(
            TeamDataRequestPayload.class,
            TideboundSettingsUpdatePayload.class,
            SharedDiscoveryRequestPayload.class,
            SatchelRequestPayload.class
    );

    @Test
    void clientRequestsCannotCarryCanonicalSpecimenOrMomentumState() {
        for (Class<?> payload : CLIENT_TO_SERVER_PAYLOADS) {
            assertTrue(payload.isRecord(), payload.getName() + " must remain an explicit request record");
            String wireTypes = Arrays.stream(payload.getRecordComponents())
                    .map(RecordComponent::getGenericType)
                    .map(type -> type.getTypeName().toLowerCase())
                    .reduce("", (left, right) -> left + " " + right);

            assertFalse(wireTypes.contains("specimendata"),
                    payload.getName() + " lets a client submit canonical specimen state");
            assertFalse(wireTypes.contains("traitmomentum"),
                    payload.getName() + " lets a client submit Trait Momentum state");
            assertFalse(wireTypes.contains("itemstack"),
                    payload.getName() + " lets a client submit an authored fish ItemStack");
        }
    }
}
