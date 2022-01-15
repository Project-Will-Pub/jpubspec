package xyz.rk0cc.willpub.pubspec.data;

import xyz.rk0cc.josev.SemVer;
import xyz.rk0cc.willpub.exceptions.pubspec.*;
import xyz.rk0cc.willpub.pubspec.data.dependencies.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * A <a href="https://en.wikipedia.org/wiki/Memento_pattern">memento</a>-liked object which archive current
 * {@link Pubspec} state data to immutable snapshot.
 * <br/>
 * Unlike {@link Pubspec}, it can not be serialized by
 * {@link com.fasterxml.jackson.databind.ObjectMapper#writeValue(File, Object)} since it aims to archive state of data,
 * not a final result which write to <code>pubspec.yaml</code>.
 * <br/>
 * To get a snapshot, the one and only way is invoking {@link #getSnapshotOfCurrentPubspec(Pubspec)}. To restore data,
 * it provides either {@link #recoverPubspec(Pubspec)} or {@link #getMutableFromSnapshot(PubspecSnapshot)} depending on
 * different scenario.
 *
 * @since 1.0.0
 */
@SuppressWarnings("ClassCanBeRecord")
public final class PubspecSnapshot implements PubspecStructure, Serializable {
    private final String name, description, publishTo;
    private final SemVer version;
    private final PubspecEnvironment environment;
    private final URL homepage, repository, issueTracker, documentation;
    private final ImportedReferenceSet dependencies, devDependencies;
    private final OverrideReferenceSet dependencyOverrides;
    private final Map<String, Object> additionalData;

    /**
     * Create snapshot from existing {@link Pubspec}.
     *
     * @param name Package name.
     * @param environment Package given {@link PubspecEnvironment}.
     * @param version Package version.
     * @param publishTo Package publish repository.
     * @param description Package description.
     * @param homepage Package homepage.
     * @param repository Package repository.
     * @param issueTracker Package issue tracker.
     * @param documentation Package documentation.
     * @param dependencies Package dependencies.
     * @param devDependencies Package develop dependencies.
     * @param dependencyOverrides Package dependencies which uses to be overridden.
     * @param additionalData Package additional data.
     *
     * @see Pubspec#Pubspec(String, PubspecEnvironment, SemVer, String, String, URL, URL, URL, URL, ImportedReferenceSet, ImportedReferenceSet, OverrideReferenceSet, Map) Same structre but detailed parameter descriptions.
     */
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

    /**
     * Package name in this snapshot.
     *
     * @return Package name.
     */
    @Nonnull
    @Override
    public String name() {
        return name;
    }

    /**
     * Environment of this snapshot.
     *
     * @return Environment of this snapshot.
     */
    @Nonnull
    @Override
    public PubspecEnvironment environment() {
        return environment;
    }

    /**
     * Applied version of this snapshot.
     *
     * @return Package version of this snapshot.
     */
    @Nullable
    @Override
    public SemVer version() {
        return version;
    }

    /**
     * Description when taking a snapshot.
     *
     * @return Packages description on this snapshot.
     */
    @Nullable
    @Override
    public String description() {
        return description;
    }

    /**
     * Repository which would be published in this snapshot.
     *
     * @return Publish repository.
     */
    @Nullable
    @Override
    public String publishTo() {
        return publishTo;
    }

    /**
     * Homepage of this snapshot.
     *
     * @return URL of homepage.
     */
    @Nullable
    @Override
    public URL homepage() {
        return homepage;
    }

    /**
     * Repository in this snapshot.
     *
     * @return URL of repository.
     */
    @Nullable
    @Override
    public URL repository() {
        return repository;
    }

    /**
     * Issue tracker in this snapshot.
     *
     * @return URL of issue tracker.
     */
    @Nullable
    @Override
    public URL issueTracker() {
        return issueTracker;
    }

    /**
     * Documentation of this snapshot.
     *
     * @return URL of documentation.
     */
    @Nullable
    @Override
    public URL documentation() {
        return documentation;
    }

    /**
     * An unmodifiable {@link DependenciesReferenceSet} of imported dependencies in this snapshot.
     *
     * @return A set of dependencies which disallow editing.
     */
    @Nonnull
    @Override
    public ImportedReferenceSet dependencies() {
        return dependencies;
    }

    /**
     * An unmodifiable {@link DependenciesReferenceSet} of imported development dependencies in this snapshot.
     *
     * @return A set of dependencies which disallow editing.
     */
    @Nonnull
    @Override
    public ImportedReferenceSet devDependencies() {
        return devDependencies;
    }

    /**
     * An unmodifiable {@link DependenciesReferenceSet} of imported dependencies in this snapshot.
     *
     * @return A set of dependencies which disallow editing.
     */
    @Nonnull
    @Override
    public OverrideReferenceSet dependencyOverrides() {
        return dependencyOverrides;
    }

    /**
     * An unmodifiable {@link Map} of additional data of this snapshot.
     *
     * @return A {@link Map} of additional data.
     */
    @Nonnull
    @Override
    public Map<String, Object> additionalData() {
        return additionalData;
    }

    /**
     * Recover {@link Pubspec} to this snapshot.
     *
     * @param pubspec A {@link Pubspec} which recover back with this snapshot.
     *
     * @see #getMutableFromSnapshot(PubspecSnapshot)
     */
    @SuppressWarnings("UseBulkOperation")
    public void recoverPubspec(@Nonnull Pubspec pubspec) {
        try {
            pubspec.modifyName(name);
            pubspec.modifyEnvironment(environment);
            pubspec.modifyPublishTo(publishTo);
            pubspec.modifyDescription(description);
            pubspec.modifyHomepage(homepage);
            pubspec.modifyRepository(repository);
            pubspec.modifyIssueTracker(issueTracker);
            pubspec.modifyDocumentation(documentation);

            ImportedReferenceSet dep = pubspec.dependencies();
            ImportedReferenceSet devDep = pubspec.devDependencies();
            OverrideReferenceSet depOr = pubspec.dependencyOverrides();

            dep.clear();
            devDep.clear();
            depOr.clear();

            dependencies.forEach(dep::add);
            devDependencies.forEach(devDep::add);
            dependencyOverrides.forEach(depOr::add);

            pubspec.clearAllAdditionalData();

            additionalData.forEach(pubspec::modifyAdditionalData);
        } catch (IllegalPubspecConfigurationException e) {
            throw new AssertionError("Unexpected illegal pubspec configuration exception thrown", e);
        }
    }

    /**
     * Take a snapshot of given {@link Pubspec}, then generated {@link PubspecSnapshot} which contains {@link Pubspec}
     * data in current state. And no changes if {@link Pubspec} applied new state.
     *
     * @param pubspec A {@link Pubspec} that want to get a snapshot.
     *
     * @return A snapshot which referencing the state of {@link Pubspec} when it invoked.
     */
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

    /**
     * Generate new mutable {@link Pubspec} from this snapshot.
     * <br/>
     * Unlike {@link #recoverPubspec(Pubspec)}, it creates a completely new {@link Pubspec} object which does not
     * affect existed one.
     *
     * @param snapshot A snapshot which will be uses to create new mutable {@link Pubspec}.
     *
     * @return A new {@link Pubspec} which using snapshot data.
     * 
     * @see #recoverPubspec(Pubspec)
     */
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
