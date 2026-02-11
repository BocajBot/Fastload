package io.github.bumblesoftware.fastload.common;

import java.util.List;

import static io.github.bumblesoftware.fastload.common.FLCommonEvents.Events.INTEGER_EVENT;
import static io.github.bumblesoftware.fastload.common.FLCommonEvents.Locations.PREPARE_START_REGION;

public class FLCommonHandler {
    public static void init() {}

    static {
        INTEGER_EVENT.registerStatic(1, List.of(PREPARE_START_REGION),
                // Skip the vanilla "wait until all non-full chunks are loaded" gate.
                (eventContext, eventStatus, event, eventArgs) -> eventContext.setHeldObj(0)
        );
    }
}
