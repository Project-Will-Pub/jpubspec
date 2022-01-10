package xyz.rk0cc.willpub.pubspec.data.dependencies;

import xyz.rk0cc.josev.constraint.pub.PubConstraintPattern;
import xyz.rk0cc.willpub.pubspec.data.dependencies.type.*;

import javax.annotation.Nonnull;

public final class OverrideReferenceSet extends DependenciesReferenceSet {
    public OverrideReferenceSet() {
        super();
    }

    public OverrideReferenceSet(@Nonnull DependenciesReferenceSet references) {
        super(references);
        assert references.stream().allMatch(OverrideReferenceSet::mustBeAbsoluteVC);
    }

    @Override
    boolean isAllowToAdd(@Nonnull DependencyReference dependencyReference) {
        return mustBeAbsoluteVC(dependencyReference);
    }

    @Nonnull
    @Override
    public OverrideReferenceSet clone() {
        return new OverrideReferenceSet(this);
    }

    private static boolean mustBeAbsoluteVC(@Nonnull DependencyReference dependencyReference) {
        if (dependencyReference instanceof VersionConstrainedDependency vcd)
            return vcd.versionConstraint().constraintPattern() == PubConstraintPattern.ABSOLUTE;

        return true;
    }
}
