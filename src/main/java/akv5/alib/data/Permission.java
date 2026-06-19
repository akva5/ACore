package akv5.alib.data;

import java.util.stream.Stream;

@SuppressWarnings("NonAsciiCharacters")
public enum Permission {
    CMI_COMMAND_FLY(false, false),
    CMI_COMMAND_WALKSPEED(false, false),
    CMI_COMMAND_FLYSPEED(false, false),
    CMI_SEEVANISHED(false, false),

    ACORE_BYPASS_COOLDOWN_COMMAND(false, false),
    ACORE_BYPASS_LIMIT_COMMAND(false, false);

    private final String value;
    private final boolean strict;
    private final boolean isAdminSkip;

    Permission(boolean strict, boolean isAdminSkip) {
        this.value = this.name().toLowerCase().replace("_", ".");
        this.strict = strict;
        this.isAdminSkip = isAdminSkip;
    }

    public String getValue() {
        return value;
    }

    public boolean isStrict() {
        return strict;
    }

    public boolean isAdminSkip() {
        return isAdminSkip;
    }

    public static Permission getEnum(String permission) {
        return Stream.of(values())
                .filter(list -> list.value.equalsIgnoreCase(permission.replace("_", ".")))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Wrong permission! Permission: " + permission));
    }
}