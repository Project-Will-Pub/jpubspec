package xyz.rk0cc.willpub.pubspec.parser;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public enum PubspecParsePreference {
    SUCCINCT_THIRD_PARTY_HOSTED_FORMAT;

    private static final EnumSet<PubspecParsePreference> enabledPreference
            = EnumSet.allOf(PubspecParsePreference.class);

    public static boolean enable(@Nonnull PubspecParsePreference preference) {
        return enabledPreference.add(preference);
    }

    public static boolean disable(@Nonnull PubspecParsePreference preference) {
        return enabledPreference.remove(preference);
    }

    public static boolean isEnabled(@Nonnull PubspecParsePreference preference) {
        return enabledPreference.contains(preference);
    }
}
