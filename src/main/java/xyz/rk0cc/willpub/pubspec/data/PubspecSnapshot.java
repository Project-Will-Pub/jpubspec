package xyz.rk0cc.willpub.pubspec.data;

import xyz.rk0cc.josev.SemVer;
import xyz.rk0cc.willpub.exceptions.pubspec.*;
import xyz.rk0cc.willpub.pubspec.data.dependencies.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

@SuppressWarnings("ClassCanBeRecord")
public final class PubspecSnapshot implements PubspecStructure {
    private final String name, description, publishTo;
    private final SemVer version;
    private final PubspecEnvironment environment;
    private final URL homepage, repository, issueTracker, documentation;
    private final ImportedReferenceSet dependencies, devDependencies;
    private final OverrideReferenceSet dependencyOverrides;
    private final Map<String, Object> additionalData;

    private PubspecSnapshot(
            @Nonnull String name,
            @Nonnull PubspecEnvironment environment,
            @Nullable SemVer version,
            @Nullable String publishTo,
            @Nullable String description,
            @Nullable URL homepage,
            @Nullable URL repository,
            @Nullable URL issueTracker,
            @Nullable URL documentation,
            @Nonnull ImportedReferenceSet dependencies,
            @Nonnull ImportedReferenceSet devDependencies,
            @Nonnull OverrideReferenceSet dependencyOverrides,
            @Nonnull Map<String, Object> additionalData
    ) {
        assert dependencies.isUnmodifiable();
        assert devDependencies.isUnmodifiable();
        assert dependencyOverrides.isUnmodifiable();
        this.name = name;
        this.environment = environment;
        this.version = version;
        this.publishTo = publishTo;
        this.description = description;
        this.homepage = homepage;
        this.repository = repository;
        this.issueTracker = issueTracker;
        this.documentation = documentation;
        this.dependencies = dependencies;
        this.devDependencies = devDependencies;
        this.dependencyOverrides = dependencyOverrides;
        this.additionalData = Collections.unmodifiableMap(additionalData);
    }

    @Nonnull
    @Override
    public String name() {
        return name;
    }

    @Nonnull
    @Override
    public PubspecEnvironment environment() {
        return environment;
    }

    @Nullable
    @Override
    public SemVer version() {
        return version;
    }

    @Nullable
    @Override
    public String description() {
        return description;
    }

    @Nullable
    @Override
    public String publishTo() {
        return publishTo;
    }

    @Nullable
    @Override
    public URL homepage() {
        return homepage;
    }

    @Nullable
    @Override
    public URL repository() {
        return repository;
    }

    @Nullable
    @Override
    public URL issueTracker() {
        return issueTracker;
    }

    @Nullable
    @Override
    public URL documentation() {
        return documentation;
    }

    @Nonnull
    @Override
    public ImportedReferenceSet dependencies() {
        return dependencies;
    }

    @Nonnull
    @Override
    public ImportedReferenceSet devDependencies() {
        return devDependencies;
    }

    @Nonnull
    @Override
    public OverrideReferenceSet dependencyOverrides() {
        return dependencyOverrides;
    }

    @Nonnull
    @Override
    public Map<String, Object> additionalData() {
        return additionalData;
    }

    @Nonnull
    public static PubspecSnapshot getSnapshotOfCurrentPubspec(@Nonnull Pubspec pubspec) {
        try {
            return new PubspecSnapshot(
                    pubspec.name(),
                    pubspec.environment(),
                    pubspec.version(),
                    pubspec.publishTo(),
                    pubspec.description(),
                    pubspec.homepage(),
                    pubspec.repository(),
                    pubspec.issueTracker(),
                    pubspec.documentation(),
                    new ImportedReferenceSet(pubspec.dependencies(), true),
                    new ImportedReferenceSet(pubspec.devDependencies(), true),
                    new OverrideReferenceSet(pubspec.dependencyOverrides(), true),
                    pubspec.additionalData()
            );
        } catch (IllegalVersionConstraintException e) {
            throw new AssertionError("Unexpected version constraint exception when cloning overrides", e);
        }
    }

    @Nonnull
    public static Pubspec getMutableFromSnapshot(@Nonnull PubspecSnapshot snapshot) {
        try {
            return new Pubspec(
                    snapshot.name(),
                    snapshot.environment(),
                    snapshot.version(),
                    snapshot.publishTo(),
                    snapshot.description(),
                    snapshot.homepage(),
                    snapshot.repository(),
                    snapshot.issueTracker(),
                    snapshot.documentation(),
                    new ImportedReferenceSet(snapshot.dependencies(), false),
                    new ImportedReferenceSet(snapshot.devDependencies(), false),
                    new OverrideReferenceSet(snapshot.dependencyOverrides(), false),
                    snapshot.additionalData()
            );
        } catch (IllegalPubspecConfigurationException e) {
            throw new AssertionError(
                    "Unexpected illegal pubspec configuration exception when cloning overrides",
                    e
            );
        }
    }
}
