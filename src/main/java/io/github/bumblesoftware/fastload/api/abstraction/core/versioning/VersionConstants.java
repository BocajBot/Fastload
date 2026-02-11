package io.github.bumblesoftware.fastload.api.abstraction.core.versioning;

import io.github.bumblesoftware.fastload.api.abstraction.def.VersionUtils;

public class VersionConstants {
    public static void init() {}

    private static final VersionUtil MINECRAFT = VersionUtils.MINECRAFT;
    public static final boolean IS_MINECRAFT_12111;


    static {
        IS_MINECRAFT_12111 = MINECRAFT.matchesAny("1.21.11");
    }
}
