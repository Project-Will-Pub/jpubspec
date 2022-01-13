package xyz.rk0cc.willpub.pubspec.parser;

import xyz.rk0cc.josev.SemVer;
import xyz.rk0cc.josev.SemVerRangeNode;
import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.function.Predicate;

public enum PubspecParsePreference {
    SUCCINCT_THIRD_PARTY_HOSTED_FORMAT(sdkc -> {
        SemVerRangeNode sdkStart = sdkc.start();

        SemVer supportedSince = new SemVer(2, 15);

        return sdkStart.orEquals()
                ? sdkStart.semVer().isGreaterOrEquals(supportedSince)
                : sdkStart.semVer().isGreater(supportedSince);
    });

    private final Predicate<PubSemVerConstraint> sdkCondition;

    PubspecParsePreference(@Nonnull Predicate<PubSemVerConstraint> sdkCondition) {
        this.sdkCondition = sdkCondition;
    }

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

    public static boolean eligible(@Nonnull PubspecParsePreference preference, @Nonnull PubSemVerConstraint sdk) {
        return isEnabled(preference) && preference.sdkCondition.test(sdk);
    }
}
