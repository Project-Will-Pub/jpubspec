package xyz.rk0cc.willpub.pubspec.data;

import xyz.rk0cc.josev.*;
import xyz.rk0cc.willpub.exceptions.pubspec.*;
import xyz.rk0cc.willpub.pubspec.data.dependencies.*;
import xyz.rk0cc.willpub.pubspec.parser.PubspecParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class Pubspec implements PubspecStructure {
    private String name, description, publishTo;
    private SemVer version;
    private PubspecEnvironment environment;
    private URL homepage, repository, issueTracker, documentation;
    private final ImportedReferenceSet dependencies, devDependencies;
    private final OverrideReferenceSet dependencyOverrides;
    private final Map<String, Object> additionalData;

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
        this.additionalData = additionalData == null ? new LinkedHashMap<>() : new LinkedHashMap<>(additionalData);
    }

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

    public void modifyName(@Nonnull String name) throws IllegalPubPackageNamingException {
        PubspecValueValidator.ValueAssertion.assertPackageNaming(name);
        this.name = name;
    }

    public void modifyEnvironment(@Nonnull PubspecEnvironment environment) {
        this.environment = environment;
    }

    public void modifyVersion(@Nullable SemVer version) {
        this.version = version;
    }

    public void modifyVersion(@Nullable String version) throws NonStandardSemVerException {
        this.modifyVersion(version == null ? null : SemVer.parse(version));
    }

    public void modifyPublishTo(@Nullable String publishTo) {
        this.publishTo = publishTo;
    }

    public void modifyDescription(@Nullable String description) {
        this.description = description;
    }

    public void modifyHomepage(@Nullable URL homepage) {
        if (homepage != null && !PubspecValueValidator.httpProtocolOnly(homepage))
            throw new IllegalArgumentException("URL must be either HTTP or HTTPS");

        this.homepage = homepage;
    }

    public void modifyHomepage(@Nullable String homepage) throws MalformedURLException {
        this.modifyHomepage(homepage == null ? null : new URL(homepage));
    }

    public void modifyRepository(@Nullable URL repository) {
        if (repository != null && !PubspecValueValidator.httpProtocolOnly(repository))
            throw new IllegalArgumentException("URL must be either HTTP or HTTPS");

        this.repository = repository;
    }

    public void modifyRepository(@Nullable String repository) throws MalformedURLException {
        this.modifyRepository(repository == null ? null : new URL(repository));
    }

    public void modifyIssueTracker(@Nullable URL issueTracker) {
        if (issueTracker != null && !PubspecValueValidator.httpProtocolOnly(issueTracker))
            throw new IllegalArgumentException("URL must be either HTTP or HTTPS");

        this.issueTracker = issueTracker;
    }

    public void modifyIssueTracker(@Nullable String issueTracker) throws MalformedURLException {
        this.modifyIssueTracker(issueTracker == null ? null : new URL(issueTracker));
    }

    public void modifyDocumentation(@Nullable URL documentation) {
        if (documentation != null && !PubspecValueValidator.httpProtocolOnly(documentation))
            throw new IllegalArgumentException("URL must be either HTTP or HTTPS");

        this.documentation = documentation;
    }

    public void modifyDocumentation(@Nullable String documentation) throws MalformedURLException {
        this.modifyDocumentation(documentation == null ? null : new URL(documentation));
    }

    public boolean appendAdditionalData(@Nonnull String key, @Nullable Object value) {
        if (PubspecParser.PUBSPEC_YAML_FIELD.contains(key))
            throw new IllegalArgumentException("'" + key + "' is not an additional field in pubspec");
        assert isJsonLikedObject(value);
        additionalData.putIfAbsent(key, value);
        return additionalData.containsKey(key);
    }

    public boolean modifyAdditionalData(@Nonnull String key, @Nullable Object value) {
        if (PubspecParser.PUBSPEC_YAML_FIELD.contains(key))
            throw new IllegalArgumentException("'" + key + "' is not an additional field in pubspec");
        assert isJsonLikedObject(value);
        additionalData.put(key, value);
        return additionalData.containsKey(key);
    }

    public boolean removeAdditionalData(@Nonnull String key) {
        if (PubspecParser.PUBSPEC_YAML_FIELD.contains(key))
            throw new IllegalArgumentException("'" + key + "' is not an additional field in pubspec");
        if (!additionalData.containsKey(key)) return false;
        additionalData.remove(key);
        return true;
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

    @Nullable
    public Object additionalDataValue(@Nonnull String key) {
        if (PubspecParser.PUBSPEC_YAML_FIELD.contains(key))
            throw new IllegalArgumentException("'" + key + "' is not an additional field in pubspec");
        return additionalData.get(key);
    }

    @Nonnull
    @Override
    public Map<String, Object> additionalData() {
        return Collections.unmodifiableMap(additionalData);
    }

    public void clearAllAdditionalData() {
        additionalData.clear();
    }

    private static boolean isJsonLikedObject(@Nullable Object value) {
        return PermitAdditionalMapValue.isJsonLiked(value);
    }
}

final class PermitAdditionalMapValue {
    private static final Stream<Class<?>> JSON_LIKED_TYPE = Stream.of(
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
            String.class
    );

    private PermitAdditionalMapValue() {}

    private static final Predicate<Object> conditionDispatcher = i -> {
        if (i instanceof List<?> nl)
            return isJsonLikedArray(nl);
        else if (i instanceof Map<?, ?> nm)
            return isJsonLikedMap(nm);
        return isJsonLikedDataType(i);
    };

    private static boolean isJsonLikedDataType(@Nullable Object v) {
        if (v == null) return true;
        return JSON_LIKED_TYPE.anyMatch(jlt -> jlt.equals(v.getClass()));
    }

    private static boolean isJsonLikedArray(@Nonnull List<?> lv) {
        return lv.stream().allMatch(conditionDispatcher);
    }

    private static boolean isJsonLikedMap(@Nonnull Map<?, ?> mv) {
        return mv.keySet().stream().allMatch(k -> k instanceof String)
                && mv.values().stream().allMatch(conditionDispatcher);
    }

    static boolean isJsonLiked(@Nullable Object incomingValue) {
        return conditionDispatcher.test(incomingValue);
    }
}