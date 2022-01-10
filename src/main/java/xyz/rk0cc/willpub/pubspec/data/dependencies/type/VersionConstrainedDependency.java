package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

public sealed interface VersionConstrainedDependency extends Serializable
        permits HostedReference, SDKReference, ThirdPartyHostedReference {
    @Nonnull
    PubSemVerConstraint versionConstraint();

    @Nonnull
    VersionConstrainedDependency changeVersionConstraint(@Nonnull PubSemVerConstraint versionConstraint);

    @Nonnull
    default VersionConstrainedDependency changeVersionConstraint(@Nullable String versionConstraint) {
        return changeVersionConstraint(PubSemVerConstraint.parse(versionConstraint));
    }

    @Nonnull
    default VersionConstrainedDependency changeVersionConstraint() {
        return changeVersionConstraint(PubSemVerConstraint.parse(null));
    }
}
