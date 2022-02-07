package xyz.rk0cc.willpub.pubspec.parser;

import xyz.rk0cc.josev.SemVer;
import xyz.rk0cc.josev.SemVerRangeNode;
import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * Preference when exporting {@link xyz.rk0cc.willpub.pubspec.data.Pubspec} to <code>pubspec.yaml</code>.
 * <br/>
 * All affected preferences must be both {@link #isEnabled(PubspecParsePreference) enabled} and meet required
 * {@link xyz.rk0cc.willpub.pubspec.data.PubspecEnvironment#sdk() Dart SDK constraint}. By default, all preferences
 * are enabled. Therefore, it provides {@link #enable(PubspecParsePreference)} and
 * {@link #disable(PubspecParsePreference)} static method to standardize preferred format of <code>pubspec.yaml</code>.
 *
 * @since 1.0.0
 */
public enum PubspecParsePreference {
    /**
     * Succinct {@link xyz.rk0cc.willpub.pubspec.data.dependencies.type.ThirdPartyHostedReference} data when the package
     * name is the same from the hosted repository.
     * <br/>
     * When it enabled, the hosted field will become a URL of repository instead of an object. It's supported since
     * Dart 2.15, any older version of Dart SDK in <code>pubspec.yaml</code> will not apply when export.
     * <br/>
     * <h2>Export preview:</h2>
     * <h3>Disabled</h3>
     * <pre>
     * foo:
     *   hosted:
     *     name: foo
     *     url: https://example.com
     *   version: ^1.0.0
     * </pre>
     * <h3>Enabled</h3>
     * <pre>
     * foo:
     *   hosted: https://example.com
     *   version: ^1.0.0
     * </pre>
     */
    SUCCINCT_THIRD_PARTY_HOSTED_FORMAT(sdkc -> {
        SemVerRangeNode sdkStart = sdkc.start();

        SemVer supportedSince = new SemVer(2, 15);

        return sdkStart.orEquals()
                ? sdkStart.semVer().isGreaterOrEquals(supportedSince)
                : sdkStart.semVer().isGreater(supportedSince);
    }),
    /**
     * Display <code>platforms</code> field even {@link xyz.rk0cc.willpub.pubspec.data.PubspecPlatforms} set all
     * <code>true</code>.
     * <br/>
     * This option required Dart 2.16 or later in SDK constraint and disabled by default.
     */
    SHOW_PLATFORMS_ENTRY_WHEN_ALL_PLATFORM_SUPPORTED(sdkc -> {
        SemVerRangeNode sdkStart = sdkc.start();

        SemVer supportedSince = new SemVer(2, 16);

        return sdkStart.orEquals()
                ? sdkStart.semVer().isGreaterOrEquals(supportedSince)
                : sdkStart.semVer().isGreater(supportedSince);
    });

    /**
     * Condition of Dart SDK requirement.
     */
    private final Predicate<PubSemVerConstraint> sdkCondition;

    /**
     * Assign preference with the condition.
     *
     * @param sdkCondition Giving Dart SDK's {@link PubSemVerConstraint} to check is
     *                     {@link #eligible(PubspecParsePreference, PubSemVerConstraint)} when enabled.
     */
    PubspecParsePreference(@Nonnull Predicate<PubSemVerConstraint> sdkCondition) {
        this.sdkCondition = sdkCondition;
    }

    /**
     * {@link EnumSet} which contains enabled preferences.
     */
    private static final EnumSet<PubspecParsePreference> enabledPreference
            = EnumSet.of(SUCCINCT_THIRD_PARTY_HOSTED_FORMAT);

    /**
     * Enable giving preference and apply it when meeting SDK constraint requirement.
     *
     * @param preference A preference which want to be activated.
     *
     * @return <code>true</code> if modified.
     */
    public static boolean enable(@Nonnull PubspecParsePreference preference) {
        return enabledPreference.add(preference);
    }

    /**
     * Disable the preference, no matter is met Dart SDK requirement or not.
     *
     * @param preference A preference which unwanted to apply.
     *
     * @return <code>true</code> if modified.
     */
    public static boolean disable(@Nonnull PubspecParsePreference preference) {
        return enabledPreference.remove(preference);
    }

    /**
     * Providing this preference is enabled currently.
     *
     * @param preference A preference that want to be checked it's state.
     *
     * @return <code>true</code> if enabled already.
     */
    public static boolean isEnabled(@Nonnull PubspecParsePreference preference) {
        return enabledPreference.contains(preference);
    }

    /**
     * Giving targeted preference with Dart SDK constraint, to determine is eligible to using this preference or not.
     *
     * @param preference Scoped preference for incoming operation.
     * @param sdk Dart SDK constant.
     *
     * @return <code>true</code> if enabled and met the constraint requirement.
     */
    static boolean eligible(@Nonnull PubspecParsePreference preference, @Nonnull PubSemVerConstraint sdk) {
        return isEnabled(preference) && preference.sdkCondition.test(sdk);
    }
}
