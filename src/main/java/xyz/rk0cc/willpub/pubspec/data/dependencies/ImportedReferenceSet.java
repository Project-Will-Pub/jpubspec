package xyz.rk0cc.willpub.pubspec.data.dependencies;

import xyz.rk0cc.willpub.pubspec.data.dependencies.type.DependencyReference;

import javax.annotation.Nonnull;

public final class ImportedReferenceSet extends DependenciesReferenceSet {
    public ImportedReferenceSet() {
        super();
    }

    public ImportedReferenceSet(@Nonnull DependenciesReferenceSet references) {
        super(references);
    }

    @Override
    boolean isAllowToAdd(@Nonnull DependencyReference dependencyReference) {
        return true;
    }

    @Nonnull
    @Override
    public ImportedReferenceSet clone() {
        return new ImportedReferenceSet(this);
    }
}
