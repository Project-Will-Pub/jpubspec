package xyz.rk0cc.willpub.pubspec.data.dependencies;

import xyz.rk0cc.josev.constraint.pub.*;
import xyz.rk0cc.willpub.exceptions.pubspec.IllegalVersionConstraintException;
import xyz.rk0cc.willpub.pubspec.data.dependencies.type.*;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Subclass of {@link DependenciesReferenceSet} that the {@link DependencyReference dependencies} will be overridden
 * on attached {@link xyz.rk0cc.willpub.pubspec.data.Pubspec}.
 *
 * @since 1.0.0
 */
public final class OverrideReferenceSet extends DependenciesReferenceSet {
    /**
     * Create an empty {@link OverrideReferenceSet}.
     */
    public OverrideReferenceSet() {
        super();
    }

    /**
     * Create new {@link OverrideReferenceSet} with existed reference.
     *
     * @param references Origin reference.
     * @param unmodifiable Set as unmodifiable mode.
     *
     * @throws IllegalVersionConstraintException If origin reference's {@link DependencyReference dependencies} has
     *                                           {@link PubSemVerConstraint} which is not using
     *                                           {@link PubConstraintPattern#ABSOLUTE} pattern.
     */
    public OverrideReferenceSet(@Nonnull DependenciesReferenceSet references, boolean unmodifiable)
            throws IllegalVersionConstraintException {
        super(references, unmodifiable);

        if (!(references instanceof OverrideReferenceSet)) {
            // Check all versioned package must be absolute if come from other reference set.
            Optional<PubSemVerConstraint> firstNonAbs = references.stream()
                    .filter(dr -> !mustBeAbsoluteVC(dr))
                    .map(navc -> ((VersionConstrainedDependency<?>) navc).versionConstraint())
                    .findFirst();

            if (firstNonAbs.isPresent())
                throw new IllegalVersionConstraintException(
                        "Overriding versioned constraint reference must be absolute",
                        firstNonAbs.get()
                );
        }
    }

    /**
     * Create new modifiable {@link OverrideReferenceSet} with existed reference.
     *
     * @param references Origin reference.
     *
     * @throws IllegalVersionConstraintException If origin reference's {@link DependencyReference dependencies} has
     *                                           {@link PubSemVerConstraint} which is not using
     *                                           {@link PubConstraintPattern#ABSOLUTE} pattern.
     */
    public OverrideReferenceSet(@Nonnull DependenciesReferenceSet references) throws IllegalVersionConstraintException {
        this(references, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean isAllowToAdd(@Nonnull DependencyReference dependencyReference) {
        return mustBeAbsoluteVC(dependencyReference);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public OverrideReferenceSet clone() {
        try {
            return new OverrideReferenceSet(this, this.isUnmodifiable());
        } catch (IllegalVersionConstraintException e) {
            throw new AssertionError(
                    "Unexpected illegal version constraint exception thrown when cloning reference",
                    e
            );
        }
    }

    /**
     * Assertion on single {@link DependencyReference}. If the reference is {@link VersionConstrainedDependency},
     * {@link VersionConstrainedDependency#versionConstraint()} must be {@link PubConstraintPattern#ABSOLUTE}.
     *
     * @param dependencyReference A reference that is going to validation.
     *
     * @return <code>true</code> if validated.
     */
    private static boolean mustBeAbsoluteVC(@Nonnull DependencyReference dependencyReference) {
        if (dependencyReference instanceof VersionConstrainedDependency vcd)
            return vcd.versionConstraint().constraintPattern() == PubConstraintPattern.ABSOLUTE;

        return true;
    }
}
