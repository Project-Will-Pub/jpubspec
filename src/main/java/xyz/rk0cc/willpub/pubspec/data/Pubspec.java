package xyz.rk0cc.willpub.pubspec.data;

import xyz.rk0cc.josev.*;
import xyz.rk0cc.willpub.exceptions.pubspec.*;
import xyz.rk0cc.willpub.pubspec.data.dependencies.*;
import xyz.rk0cc.willpub.pubspec.utils.PubspecValueValidator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
        this.additionalData = additionalData == null ? new HashMap<>() : new HashMap<>(additionalData);
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
            throw new HTTPOnlyURLException("Homepage");

        this.homepage = homepage;
    }

    public void modifyHomepage(@Nullable String homepage) throws MalformedURLException {
        this.modifyHomepage(homepage == null ? null : new URL(homepage));
    }

    public void modifyRepository(@Nullable URL repository) {
        if (repository != null && !PubspecValueValidator.httpProtocolOnly(repository))
            throw new HTTPOnlyURLException("Repository");

        this.repository = repository;
    }

    public void modifyRepository(@Nullable String repository) throws MalformedURLException {
        this.modifyRepository(repository == null ? null : new URL(repository));
    }

    public void modifyIssueTracker(@Nullable URL issueTracker) {
        if (issueTracker != null && !PubspecValueValidator.httpProtocolOnly(issueTracker))
            throw new HTTPOnlyURLException("Issue tracker");

        this.issueTracker = issueTracker;
    }

    public void modifyIssueTracker(@Nullable String issueTracker) throws MalformedURLException {
        this.modifyIssueTracker(issueTracker == null ? null : new URL(issueTracker));
    }

    public void modifyDocumentation(@Nullable URL documentation) {
        if (documentation != null && !PubspecValueValidator.httpProtocolOnly(documentation))
            throw new HTTPOnlyURLException("Documentation");

        this.documentation = documentation;
    }

    public void modifyDocumentation(@Nullable String documentation) throws MalformedURLException {
        this.modifyDocumentation(documentation == null ? null : new URL(documentation));
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
}

final class HTTPOnlyURLException extends IllegalArgumentException {
    HTTPOnlyURLException(@Nonnull String urlField) {
        super(
                String.valueOf(urlField.charAt(0)).toUpperCase()
                        + urlField.substring(1)
                        + " only accept HTTP or HTTPS only"
        );
    }
}