package xyz.rk0cc.willpub.pubspec.data.dependencies;

import xyz.rk0cc.willpub.pubspec.data.dependencies.type.DependencyReference;

import javax.annotation.Nonnull;

/**
 * Subclass from {@link DependenciesReferenceSet} contains {@link DependencyReference dependencies} to attached
 * {@link xyz.rk0cc.willpub.pubspec.data.Pubspec} without any restriction.
 *
 * @since 1.0.0
 */
public final class ImportedReferenceSet extends DependenciesReferenceSet {
    /**
     * Create an empty {@link ImportedReferenceSet} and pending to modify.
     */
    public ImportedReferenceSet() {
        super();
    }

    /**
     * Create an {@link ImportedReferenceSet} from existed {@link DependenciesReferenceSet}.
     *
     * @param references Original reference.
     * @param unmodifiable Set to unmodifiable mode.
     */
    public ImportedReferenceSet(@Nonnull DependenciesReferenceSet references, boolean unmodifiable) {
        super(references, unmodifiable);
    }

    /**
     * Create an {@link ImportedReferenceSet} from existed {@link DependenciesReferenceSet}.
     *
     * @param references Original reference.
     */
    public ImportedReferenceSet(@Nonnull DependenciesReferenceSet references) {
        super(references);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean isAllowToAdd(@Nonnull DependencyReference dependencyReference) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ImportedReferenceSet clone() {
        return new ImportedReferenceSet(this, this.isUnmodifiable());
    }
}
