package xyz.rk0cc.willpub.pubspec.data;

import xyz.rk0cc.josev.*;
import xyz.rk0cc.willpub.exceptions.pubspec.*;
import xyz.rk0cc.willpub.pubspec.data.dependencies.*;
import xyz.rk0cc.willpub.pubspec.parser.PubspecParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;

/**
 * A mutable object structure to match with <code>pubspec.yaml</code>.
 * <br/>
 * This object is trying to replicate the field form <code>pubspec.yaml</code> and can be modified under JVM. And
 * integrated {@link PubspecValueValidator validator} to validate each inserted field is following the documentation.
 * The source of {@link Pubspec} can be from {@link #Pubspec(String, PubspecEnvironment, SemVer, String, String, URL, URL, URL, URL, ImportedReferenceSet, ImportedReferenceSet, OverrideReferenceSet, Map) constructor},
 * {@link PubspecParser#PUBSPEC_MAPPER mapper's} {@link com.fasterxml.jackson.databind.ObjectMapper#readValue(File, Class) reader},
 * or {@link PubspecSnapshot#getMutableFromSnapshot(PubspecSnapshot) immutable snapshot}.
 * <br/>
 * It accepted {@link #additionalData() additional field data} to allow assigning deprecated or platform unique field
 * like <code>flutter</code> or <code>executable</code> which less commonly uses but still recognized by
 * <code>pub</code>. Therefore, no {@link PubspecValueValidator validation} available for {@link #additionalData()}.
 *
 * @since 1.0.0
 *
 * @see PubspecSnapshot
 * @see <a href="https://dart.dev/tools/pub/pubspec">"The pubspec file" from dart.dev</a>
 */
public final class Pubspec implements PubspecStructure {
    private String name, description, publishTo;
    private SemVer version;
    private PubspecEnvironment environment;
    private URL homepage, repository, issueTracker, documentation;
    private final ImportedReferenceSet dependencies, devDependencies;
    private final OverrideReferenceSet dependencyOverrides;
    private final Map<String, Object> additionalData;

    /**
     * Create {@link Pubspec} data with all field provides.
     *
     * @param name Package name (mandatory)
     * @param environment Package environment preference (mandatory)
     * @param version Package version
     * @param publishTo Specify where package published, <code>null</code> as default and <code>"none"</code> if this
     *                  package is not consider publishing.
     * @param description Description of the package, when need to be published, it must between 60 and 180 characters
     *                    in a {@link String}.
     * @param homepage Package's homepage if applied.
     * @param repository Package's repository if applied.
     * @param issueTracker Package's issue page. If omitted and the <code>repository</code> field is pointing to GitHub,
     *                     it uses <code>&#47;issues</code> from <code>repository</code> URL on
     *                     <a href="https://pub.dev">pub.dev</a>.
     * @param documentation Package's documentation page for providing more detailed reference of this package.
     * @param dependencies A {@link DependenciesReferenceSet set} of
     *                     {@link xyz.rk0cc.willpub.pubspec.data.dependencies.type.DependencyReference reference} that
     *                     is going to uses in this package.
     * @param devDependencies A {@link DependenciesReferenceSet set} of
     *                        {@link xyz.rk0cc.willpub.pubspec.data.dependencies.type.DependencyReference reference}
     *                        which only uses during testing.
     * @param dependencyOverrides A {@link DependenciesReferenceSet set} to specify
     *                            {@link xyz.rk0cc.willpub.pubspec.data.dependencies.type.DependencyReference reference}
     *                            will be applied in package's own pubspec.
     * @param additionalData A {@link Map} containing fields which not mentioned in {@link Pubspec}.
     *
     * @throws IllegalPubPackageNamingException If <code>name</code> does not meet requirement of
     *                                          {@link PubspecValueValidator#packageNaming(String) package naming}.
     */
    public Pubspec(
            @Nonnull String name,
            @Nonnull PubspecEnvironment environment,
            @Nullable SemVer version,
            @Nullable String publishTo,
            @Nullable String description,
            @Nullable URL homepage,
            @Nullable URL repository,
            @Nullable URL issueTracker,
            @Nullable URL documentation,
            @Nullable ImportedReferenceSet dependencies,
            @Nullable ImportedReferenceSet devDependencies,
            @Nullable OverrideReferenceSet dependencyOverrides,
            @Nullable Map<String, Object> additionalData
    ) throws IllegalPubPackageNamingException {
        this.modifyName(name);
        this.modifyEnvironment(environment);
        this.modifyVersion(version);
        this.modifyPublishTo(publishTo);
        this.modifyDescription(description);
        this.modifyHomepage(homepage);
        this.modifyRepository(repository);
        this.modifyIssueTracker(issueTracker);
        this.modifyDocumentation(documentation);
        this.dependencies = dependencies == null ? new ImportedReferenceSet() : new ImportedReferenceSet(dependencies);
        this.devDependencies = devDependencies == null
                ? new ImportedReferenceSet()
                : new ImportedReferenceSet(devDependencies);
        try {
            this.dependencyOverrides = dependencyOverrides == null
                    ? new OverrideReferenceSet()
                    : new OverrideReferenceSet(dependencyOverrides);
        } catch (IllegalVersionConstraintException e) {
            throw new IllegalArgumentException("Found illegal version constraint override dependency in the set.", e);
        }
        assert additionalData == null || additionalData.values().stream().allMatch(Pubspec::isJsonLikedObject);
        this.additionalData = additionalData == null ? new LinkedHashMap<>() : new LinkedHashMap<>(additionalData);
    }

    /**
     * Create new {@link Pubspec} with mandatory fields only. And any non-mandatory field set as <code>null</code>
     * (except {@link #publishTo()} which will be assigned as <code>"none"</code>).
     *
     * @param name Package name.
     * @param environment Package environment.
     *
     * @throws IllegalPubPackageNamingException If <code>name</code> does not meet requirement of
     *                                          {@link PubspecValueValidator#packageNaming(String) package naming}.
     */
    public Pubspec(@Nonnull String name, @Nonnull PubspecEnvironment environment)
            throws IllegalPubPackageNamingException {
        this(
                name,
                environment,
                null,
                "none",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    /**
     * Edit package name in this {@link Pubspec}.
     *
     * @param name New non-<code>null</code> package name.
     *
     * @throws IllegalPubPackageNamingException When package name does not follow naming policy.
     */
    public void modifyName(@Nonnull String name) throws IllegalPubPackageNamingException {
        PubspecValueValidator.ValueAssertion.assertPackageNaming(name);
        this.name = name;
    }

    /**
     * Define new {@link PubspecEnvironment environment} for {@link Pubspec}.
     *
     * @param environment New configuration of {@link PubspecEnvironment environment}.
     */
    public void modifyEnvironment(@Nonnull PubspecEnvironment environment) {
        this.environment = environment;
    }

    /**
     * Apply {@link SemVer} versioning of this package.
     *
     * @param version Package's version.
     */
    public void modifyVersion(@Nullable SemVer version) {
        this.version = version;
    }

    /**
     * Apply {@link String} of versioning of this package.
     *
     * @param version A {@link String} of package version.
     *
     * @throws NonStandardSemVerException If this <code>version</code> {@link SemVer#parse(String) parse} failed.
     */
    public void modifyVersion(@Nullable String version) throws NonStandardSemVerException {
        this.modifyVersion(version == null ? null : SemVer.parse(version));
    }

    /**
     * Edit package publish location.
     *
     * @param publishTo New location of publishing package, <code>null</code> as default and <code>"none"</code> if not
     *                  going to publish.
     */
    public void modifyPublishTo(@Nullable String publishTo) {
        this.publishTo = publishTo;
    }

    /**
     * Define package's description of this package.
     *
     * @param description New description of this package.
     */
    public void modifyDescription(@Nullable String description) {
        this.description = description;
    }

    /**
     * Locate a homepage of package.
     *
     * @param homepage {@link URL} to package homepage.
     */
    public void modifyHomepage(@Nullable URL homepage) {
        if (homepage != null && !PubspecValueValidator.httpProtocolOnly(homepage))
            throw new IllegalArgumentException("URL must be either HTTP or HTTPS");

        this.homepage = homepage;
    }

    /**
     * Locate a homepage of package.
     *
     * @param homepage A {@link String} of URL to package homepage.
     *
     * @throws MalformedURLException If {@link String} can not be {@link URL#URL(String) converted}.
     */
    public void modifyHomepage(@Nullable String homepage) throws MalformedURLException {
        this.modifyHomepage(homepage == null ? null : new URL(homepage));
    }

    /**
     * Locate a package repository.
     *
     * @param repository {@link URL} to package repository.
     */
    public void modifyRepository(@Nullable URL repository) {
        if (repository != null && !PubspecValueValidator.httpProtocolOnly(repository))
            throw new IllegalArgumentException("URL must be either HTTP or HTTPS");

        this.repository = repository;
    }

    /**
     * Locate a package repository.
     *
     * @param repository A {@link String} of URL to package repository.
     *
     * @throws MalformedURLException If {@link String} can not be {@link URL#URL(String) converted}.
     */
    public void modifyRepository(@Nullable String repository) throws MalformedURLException {
        this.modifyRepository(repository == null ? null : new URL(repository));
    }

    /**
     * Apply a location to report an issue of this package.
     *
     * @param issueTracker A {@link URL} to issue page of this package.
     */
    public void modifyIssueTracker(@Nullable URL issueTracker) {
        if (issueTracker != null && !PubspecValueValidator.httpProtocolOnly(issueTracker))
            throw new IllegalArgumentException("URL must be either HTTP or HTTPS");

        this.issueTracker = issueTracker;
    }

    /**
     * Apply a location to report an issue of this package.
     *
     * @param issueTracker A {@link String} of URL which locating issue page.
     *
     * @throws MalformedURLException If {@link String} can not be {@link URL#URL(String) converted}.
     */
    public void modifyIssueTracker(@Nullable String issueTracker) throws MalformedURLException {
        this.modifyIssueTracker(issueTracker == null ? null : new URL(issueTracker));
    }

    /**
     * Provide a {@link URL} of package documentation.
     *
     * @param documentation A {@link URL} of package documentation.
     */
    public void modifyDocumentation(@Nullable URL documentation) {
        if (documentation != null && !PubspecValueValidator.httpProtocolOnly(documentation))
            throw new IllegalArgumentException("URL must be either HTTP or HTTPS");

        this.documentation = documentation;
    }

    /**
     * Provide a {@link URL} of package documentation.
     *
     * @param documentation A {@link String} of URL which locating package documentation.
     *
     * @throws MalformedURLException If {@link String} can not be {@link URL#URL(String) converted}.
     */
    public void modifyDocumentation(@Nullable String documentation) throws MalformedURLException {
        this.modifyDocumentation(documentation == null ? null : new URL(documentation));
    }

    /**
     * Append <code>value</code> if <code>key</code> is not defined before.
     *
     * @param key Additional field name.
     * @param value Additional value which can be reference to JSON.
     *
     * @return <code>true</code> if appended.
     *
     * @throws IllegalArgumentException If <code>key</code> is referencing any provided field in {@link Pubspec}.
     * 
     * @see #modifyAdditionalData(String, Object) 
     * @see Map#putIfAbsent(Object, Object) 
     */
    public boolean appendAdditionalData(@Nonnull String key, @Nullable Object value) {
        if (PubspecParser.PUBSPEC_YAML_FIELD.contains(key))
            throw new IllegalArgumentException("'" + key + "' is not an additional field in pubspec");
        assert isJsonLikedObject(value);
        if (additionalData.containsKey(key)) return false;
        additionalData.putIfAbsent(key, value);
        return additionalData.containsKey(key);
    }

    /**
     * Assign <code>value</code> for <code>key</code>, no matter is defined or not.
     * 
     * @param key Additional field name.
     * @param value Additional value which can be reference to JSON.
     * 
     * @return <code>true</code> if modified.
     *
     * @throws IllegalArgumentException If <code>key</code> is referencing any provided field in {@link Pubspec}.
     * 
     * @see #appendAdditionalData(String, Object) 
     * @see Map#put(Object, Object)
     */
    public boolean modifyAdditionalData(@Nonnull String key, @Nullable Object value) {
        if (PubspecParser.PUBSPEC_YAML_FIELD.contains(key))
            throw new IllegalArgumentException("'" + key + "' is not an additional field in pubspec");
        assert isJsonLikedObject(value);
        additionalData.put(key, value);
        return additionalData.containsKey(key);
    }

    /**
     * Remove <code>key</code> from additional data field.
     *
     * @param key Additional field name which going to remove.
     *
     * @return <code>true</code> if removed successfully form additional data.
     *
     * @throws IllegalArgumentException If <code>key</code> is referencing any provided field in {@link Pubspec}.
     */
    public boolean removeAdditionalData(@Nonnull String key) {
        if (PubspecParser.PUBSPEC_YAML_FIELD.contains(key))
            throw new IllegalArgumentException("'" + key + "' is not an additional field in pubspec");
        if (!additionalData.containsKey(key)) return false;
        additionalData.remove(key);
        return true;
    }

    /**
     * A package name to recognize from various of packages in pub repositories.
     *
     * @return A {@link String} of package name.
     */
    @Nonnull
    @Override
    public String name() {
        return name;
    }

    /**
     * Indicate {@link PubspecEnvironment environment} that to make package worked.
     *
     * @return A Dart {@link PubspecEnvironment environment} required when imported by other packages.
     */
    @Nonnull
    @Override
    public PubspecEnvironment environment() {
        return environment;
    }

    /**
     * A package version assigned for this package.
     * <br/>
     * It can be <code>null</code> if the package is not consider publishing.
     *
     * @return Current version of this package (if applied).
     */
    @Nullable
    @Override
    public SemVer version() {
        return version;
    }

    /**
     * Describe the package function in few words to others.
     *
     * @return Description of this package.
     */
    @Nullable
    @Override
    public String description() {
        return description;
    }

    /**
     * Specify another repository to publish the package, if it set to be <code>null</code>, it reference to
     * <a href="https://pub.dev">official repository</a> in most cases and <code>"none"</code> if not decided to publish
     * this package.
     *
     * @return A repository location that using to publish this package.
     */
    @Nullable
    @Override
    public String publishTo() {
        return publishTo;
    }

    /**
     * Location of package's homepage (if applied).
     *
     * @return Package homepage {@link URL}.
     */
    @Nullable
    @Override
    public URL homepage() {
        return homepage;
    }

    /**
     * Location of package's repository (if applied).
     *
     * @return Package repository {@link URL}.
     */
    @Nullable
    @Override
    public URL repository() {
        return repository;
    }

    /**
     * Location of reporting issue of this package (if applied).
     *
     * @return Package's issue page {@link URL}.
     */
    @Nullable
    @Override
    public URL issueTracker() {
        return issueTracker;
    }

    /**
     * Location of package documentation (if applied).
     *
     * @return Package's documentation {@link URL}.
     */
    @Nullable
    @Override
    public URL documentation() {
        return documentation;
    }

    /**
     * Manage the {@link xyz.rk0cc.willpub.pubspec.data.dependencies.type.DependencyReference dependencies} are used in
     * this package.
     * <br/>
     * This package is mutable and attached with {@link Pubspec}. And can be
     * {@link DependenciesReferenceSet#clone() cloned} if not using to modify existed dependencies.
     *
     * @return An {@link ImportedReferenceSet} which contains dependencies uses on this package.
     */
    @Nonnull
    @Override
    public ImportedReferenceSet dependencies() {
        return dependencies;
    }

    /**
     * Manage the {@link xyz.rk0cc.willpub.pubspec.data.dependencies.type.DependencyReference dependencies} are used in
     * this package during development
     * <br/>
     * This package is mutable and attached with {@link Pubspec}. And can be
     * {@link DependenciesReferenceSet#clone() cloned} if not using to modify existed dependencies.
     *
     * @return An {@link ImportedReferenceSet} which contains dependencies uses for development only on this package.
     */
    @Nonnull
    @Override
    public ImportedReferenceSet devDependencies() {
        return devDependencies;
    }

    /**
     * Specify which {@link xyz.rk0cc.willpub.pubspec.data.dependencies.type.DependencyReference dependencies} will be
     * overridden on this {@link Pubspec}.
     * <br/>
     * This package is mutable and attached with {@link Pubspec}. And can be
     * {@link DependenciesReferenceSet#clone() cloned} if not using to modify existed dependencies.
     *
     * @return An {@link OverrideReferenceSet} which containing dependencies will be used instead of using from
     *         {@link #dependencies()} and {@link #devDependencies()} in package's own pubspec.
     */
    @Nonnull
    @Override
    public OverrideReferenceSet dependencyOverrides() {
        return dependencyOverrides;
    }

    /**
     * Check the <code>key</code> is assigned in additional data already.
     *
     * @param key A {@link String} of the field name.
     *
     * @return <code>true</code> if contains.
     *
     * @throws IllegalArgumentException If <code>key</code> is referencing any provided field in {@link Pubspec}.
     *
     * @see Map#containsKey(Object)
     */
    public boolean containsKeyInAdditionalData(@Nonnull String key) {
        if (PubspecParser.PUBSPEC_YAML_FIELD.contains(key))
            throw new IllegalArgumentException("'" + key + "' is not an additional field in pubspec");
        return additionalData.containsKey(key);
    }

    /**
     * Return a value which comes form additional data.
     *
     * @param key Name of additional field.
     *
     * @return Value which assigned on this key.
     *
     * @throws IllegalArgumentException If <code>key</code> is referencing any provided field in {@link Pubspec}.
     *
     * @see Map#get(Object)
     */
    @Nullable
    public Object additionalDataValue(@Nonnull String key) {
        if (PubspecParser.PUBSPEC_YAML_FIELD.contains(key))
            throw new IllegalArgumentException("'" + key + "' is not an additional field in pubspec");
        return additionalData.get(key);
    }

    /**
     * Return entire {@link Map} of additional data applied in {@link Pubspec}.
     * <br/>
     * To prevent unexpected type applied directly via {@link Map}, the returned {@link Map} is
     * {@link Collections#unmodifiableMap(Map) unmodifiable}.
     *
     * @return An unmodifiable map representing any data which does not provide setter and getter in {@link Pubspec}.
     */
    @Nonnull
    @Override
    public Map<String, Object> additionalData() {
        return Collections.unmodifiableMap(additionalData);
    }

    /**
     * Purge all assigned additional data.
     *
     * @see Map#clear()
     */
    public void clearAllAdditionalData() {
        additionalData.clear();
    }

    /**
     * Check any invoked method which modifying {@link #additionalData}'s value meet requirement.
     *
     * @param value Pending value to be assigned in {@link #additionalData}.
     *
     * @return <code>true</code> if JSON liked.
     */
    private static boolean isJsonLikedObject(@Nullable Object value) {
        return PermitAdditionalMapValue.isJsonLiked(value);
    }

    /**
     * Determine this is a <a href="flutter.dev">Flutter</a> project instead of ordinary <a href="dart.dev">Dart</a>
     * project.
     * <br/>
     * If this is Flutter project, it changes how to invoke pub commands in JVM.
     *
     * @param pubspec A {@link Pubspec} for inspecting structure.
     *
     * @return <code>true</code> if either {@link PubspecEnvironment#flutter()} is non-<code>null</code> or contains
     *         <code>flutter</code> under {@link #additionalData()}.
     */
    public static boolean isFlutterProject(@Nonnull Pubspec pubspec) {
        return pubspec.environment().flutter() != null || pubspec.additionalData.containsKey("flutter");
    }
}

/**
 * A class to validate the incoming value is a JSON liked type in Java object.
 *
 * @since 1.0.0
 */
final class PermitAdditionalMapValue {
    /**
     * A {@link Set} of {@link Class} which can be assumed as primitive type in JSON.
     */
    private static final Set<Class<?>> JSON_LIKED_TYPE = Set.of(
            Integer.class,
            int.class,
            Long.class,
            long.class,
            Float.class,
            float.class,
            Double.class,
            double.class,
            Byte.class,
            byte.class,
            Short.class,
            short.class,
            Boolean.class,
            boolean.class,
            Character.class,
            char.class,
            String.class
    );

    /**
     * A useless constructor since it only contains static value and method only.
     */
    private PermitAdditionalMapValue() {}

    /**
     * A {@link Predicate} to isolate different validation approach if dealing with collections type.
     */
    private static final Predicate<Object> conditionDispatcher = i -> {
        if (i instanceof List<?> nl)
            return isJsonLikedArray(nl);
        else if (i instanceof Map<?, ?> nm)
            return isJsonLikedMap(nm);
        return isJsonLikedDataType(i);
    };

    /**
     * Check the non-collection type is primitive for JSON.
     *
     * @param v Map's value that pending to set.
     *
     * @return <code>true</code> if one of the {@link #JSON_LIKED_TYPE} matched.
     */
    private static synchronized boolean isJsonLikedDataType(@Nullable Object v) {
        if (v == null) return true;
        return JSON_LIKED_TYPE.stream().anyMatch(jlt -> jlt.equals(v.getClass()));
    }

    /**
     * Validating all items in the {@link List} are valid.
     *
     * @param lv A {@link List} of map's value that pending to set.
     *
     * @return <code>true</code> if all items is followed.
     */
    private static boolean isJsonLikedArray(@Nonnull List<?> lv) {
        return lv.stream().allMatch(conditionDispatcher);
    }

    /**
     * Validating all items in the {@link Map} are valid.
     *
     * @param mv A {@link Map} which becomes nested of map's value that pending to set.
     *
     * @return <code>true</code> if all key are {@link String} all value is followed.
     */
    private static boolean isJsonLikedMap(@Nonnull Map<?, ?> mv) {
        return mv.keySet().stream().allMatch(k -> k instanceof String)
                && mv.values().stream().allMatch(conditionDispatcher);
    }

    /**
     * Entry point of validation.
     *
     * @param incomingValue An {@link Object} of {@link Map}'s value.
     *
     * @return <code>true</code> if the value is JSON liked.
     */
    static boolean isJsonLiked(@Nullable Object incomingValue) {
        return conditionDispatcher.test(incomingValue);
    }
}