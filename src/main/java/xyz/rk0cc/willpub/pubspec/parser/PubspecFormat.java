package xyz.rk0cc.willpub.pubspec.parser;

import xyz.rk0cc.josev.SemVer;
import xyz.rk0cc.josev.SemVerRangeNode;
import xyz.rk0cc.willpub.pubspec.data.PubspecEnvironment;

import javax.annotation.Nonnull;

public enum PubspecFormat {
    SUCCINCT_THIRD_PARTY_HOSTED_FORMAT(new SemVer(2, 15));

    private final SemVer affectSince;

    PubspecFormat(@Nonnull SemVer affectSince) {
        assert affectSince.preRelease() == null;
        this.affectSince = affectSince;
    }

    public boolean eligible(@Nonnull PubspecEnvironment environment) {
        final SemVerRangeNode sdkRN = environment.sdk().start();
        return sdkRN.orEquals()
                ? sdkRN.semVer().isGreaterOrEquals(affectSince)
                : sdkRN.semVer().isGreater(affectSince);
    }
}