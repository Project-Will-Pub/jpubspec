package xyz.rk0cc.willpub.pubspec.data;

import xyz.rk0cc.josev.SemVer;
import xyz.rk0cc.willpub.pubspec.data.dependencies.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.util.Map;

/**
 * Interface that defining <code>pubspec.yaml</code> field structure to {@link Pubspec} and {@link PubspecSnapshot}.
 *
 * @since 1.0.0
 */
interface PubspecStructure {
    /**
     * <code>name</code> in <code>pubspec.yaml</code>.
     *
     * @return Package name.
     */
    @Nonnull
    String name();

    /**
     * <code>environment</code> in <code>pubspec.yaml</code>.
     *
     * @return Package environment.
     */
    @Nonnull
    PubspecEnvironment environment();

    /**
     * <code>version</code> in <code>pubspec.yaml</code>.
     *
     * @return Package version.
     */
    @Nullable
    SemVer version();

    /**
     * <code>description</code> in <code>pubspec.yaml</code>.
     *
     * @return Package description.
     */
    @Nullable
    String description();

    /**
     * <code>publish_to</code> in <code>pubspec.yaml</code>.
     *
     * @return Package publish location.
     */
    @Nullable
    String publishTo();

    /**
     * <code>homepage</code> in <code>pubspec.yaml</code>.
     *
     * @return Package homepage.
     */
    @Nullable
    URL homepage();

    /**
     * <code>repository</code> in <code>pubspec.yaml</code>.
     *
     * @return Package repository.
     */
    @Nullable
    URL repository();

    /**
     * <code>issue_tracker</code> in <code>pubspec.yaml</code>.
     *
     * @return Package issue tracker.
     */
    @Nullable
    URL issueTracker();

    /**
     * <code>documentation</code> in <code>pubspec.yaml</code>.
     *
     * @return Package documentation.
     */
    @Nullable
    URL documentation();

    /**
     * <code>dependencies</code> in <code>pubspec.yaml</code>.
     *
     * @return Package dependencies.
     */
    @Nonnull
    ImportedReferenceSet dependencies();

    /**
     * <code>dev_dependencies</code> in <code>pubspec.yaml</code>.
     *
     * @return Package development dependencies.
     */
    @Nonnull
    ImportedReferenceSet devDependencies();

    /**
     * <code>dependency_overrides</code> in <code>pubspec.yaml</code>.
     *
     * @return Package override dependencies.
     */
    @Nonnull
    OverrideReferenceSet dependencyOverrides();

    /**
     * Display platform supported state in <code>pubspec.yaml</code>.
     *
     * @return Platformed supported state.
     */
    @Nonnull
    PubspecPlatforms platforms();

    /**
     * Fields in <code>pubspec.yaml</code> which does not declare in this interface.
     *
     * @return Additional field map data.
     */
    @Nonnull
    Map<String, Object> additionalData();
}
