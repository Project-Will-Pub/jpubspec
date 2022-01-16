package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Optional interface of {@link DependencyReference} which only uses when applying {@link PubSemVerConstraint}.
 * <br/>
 * This interface helps {@link xyz.rk0cc.willpub.pubspec.data.dependencies.OverrideReferenceSet} to determine
 * {@link PubSemVerConstraint#constraintPattern() constraint pattern} must be
 * {@link xyz.rk0cc.josev.constraint.pub.PubConstraintPattern#ABSOLUTE absolute}.
 *
 * @param <V> Implemented type of {@link VersionConstrainedDependency}.
 *
 * @since 1.0.0
 */
public sealed interface VersionConstrainedDependency<V extends VersionConstrainedDependency<V>> extends Serializable
        permits HostedReference, SDKReference, ThirdPartyHostedReference {
    /**
     * Version constraint of this dependency.
     *
     * @return Current applied {@link PubSemVerConstraint}.
     */
    @Nonnull
    PubSemVerConstraint versionConstraint();

    /**
     * Change and apply new version constraint to current {@link VersionConstrainedDependency}.
     *
     * @param versionConstraint New {@link PubSemVerConstraint} is going to apply.
     *
     * @return A reference with applied {@link PubSemVerConstraint}.
     */
    @Nonnull
    V changeVersionConstraint(@Nonnull PubSemVerConstraint versionConstraint);

    /**
     * Change and apply version constraint {@link String} to current {@link VersionConstrainedDependency}.
     *
     * @param versionConstraint New {@link PubSemVerConstraint} is going to apply.
     *
     * @return A reference with applied {@link PubSemVerConstraint}.
     */
    @Nonnull
    default V changeVersionConstraint(@Nullable String versionConstraint) {
        return changeVersionConstraint(PubSemVerConstraint.parse(versionConstraint));
    }

    /**
     * Apply new {@link PubSemVerConstraint} to "<code>any</code>"
     *
     * @return A reference with {@link PubSemVerConstraint#constraintPattern()} is
     * {@link xyz.rk0cc.josev.constraint.pub.PubConstraintPattern#ANY any}.
     */
    @Nonnull
    default V changeVersionConstraint() {
        return changeVersionConstraint(PubSemVerConstraint.parse(null));
    }
}
