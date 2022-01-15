package xyz.rk0cc.willpub.pubspec.data.dependencies;

import xyz.rk0cc.josev.constraint.pub.*;
import xyz.rk0cc.willpub.exceptions.pubspec.IllegalVersionConstraintException;
import xyz.rk0cc.willpub.pubspec.data.dependencies.type.*;

import javax.annotation.Nonnull;
import java.util.Optional;

public final class OverrideReferenceSet extends DependenciesReferenceSet {
    public OverrideReferenceSet() {
        super();
    }

    public OverrideReferenceSet(@Nonnull DependenciesReferenceSet references, boolean unmodifiable)
            throws IllegalVersionConstraintException {
        super(references, unmodifiable);

        Optional<PubSemVerConstraint> firstNonAbs = references.stream()
                .filter(dr -> !mustBeAbsoluteVC(dr))
                .map(navc -> ((VersionConstrainedDependency) navc).versionConstraint())
                .findFirst();

        if (firstNonAbs.isPresent())
            throw new IllegalVersionConstraintException(
                    "Overriding versioned constraint reference must be absolute",
                    firstNonAbs.get()
            );
    }

    public OverrideReferenceSet(@Nonnull DependenciesReferenceSet references) throws IllegalVersionConstraintException {
        super(references);
    }

    @Override
    boolean isAllowToAdd(@Nonnull DependencyReference dependencyReference) {
        return mustBeAbsoluteVC(dependencyReference);
    }

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

    private static boolean mustBeAbsoluteVC(@Nonnull DependencyReference dependencyReference) {
        if (dependencyReference instanceof VersionConstrainedDependency vcd)
            return vcd.versionConstraint().constraintPattern() == PubConstraintPattern.ABSOLUTE;

        return true;
    }
}
