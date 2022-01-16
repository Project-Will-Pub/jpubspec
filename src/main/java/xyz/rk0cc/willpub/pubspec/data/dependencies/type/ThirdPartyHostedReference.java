package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;
import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubPackageNamingException;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Referencing the dependency that does not come from default pub server.
 *
 * @since 1.0.0
 *
 * @see HostedReference
 */
public final class ThirdPartyHostedReference extends DependencyReference
        implements VersionConstrainedDependency<ThirdPartyHostedReference> {
    private final URL repositoryURL;
    private final String hostedName;
    private final PubSemVerConstraint versionConstraint;

    /**
     * Create new dependency reference from other pub repository.
     *
     * @param name Package name.
     * @param repositoryURL Package hosted pub repository URL.
     * @param hostedName Package name in hosted repository.
     * @param versionConstraint Package version constraint.
     *
     * @throws IllegalPubPackageNamingException If package name is illegal.
     */
    public ThirdPartyHostedReference(
            @Nonnull String name,
            @Nonnull URL repositoryURL,
            @Nonnull String hostedName,
            @Nonnull PubSemVerConstraint versionConstraint
    ) throws IllegalPubPackageNamingException {
        super(name);
        this.repositoryURL = repositoryURL;
        this.hostedName = hostedName;
        this.versionConstraint = versionConstraint;
    }

    /**
     * Create new dependency reference from other pub repository.
     *
     * @param name Package name.
     * @param repositoryURL Package hosted pub repository URL.
     * @param versionConstraint Package version constraint.
     *
     * @throws IllegalPubPackageNamingException If package name is illegal.
     */
    public ThirdPartyHostedReference(
            @Nonnull String name,
            @Nonnull URL repositoryURL,
            @Nonnull PubSemVerConstraint versionConstraint
    ) throws IllegalPubPackageNamingException {
        this(name, repositoryURL, name, versionConstraint);
    }

    /**
     * Create new dependency reference from other pub repository.
     *
     * @param name Package name.
     * @param repositoryURL Package hosted pub repository URL.
     * @param hostedName Package name in hosted repository.
     *
     * @throws IllegalPubPackageNamingException If package name is illegal.
     */
    public ThirdPartyHostedReference(@Nonnull String name, @Nonnull URL repositoryURL, @Nonnull String hostedName)
            throws IllegalPubPackageNamingException {
        this(name, repositoryURL, hostedName, PubSemVerConstraint.parse(null));
    }

    /**
     * Create new dependency reference from other pub repository.
     *
     * @param name Package name.
     * @param repositoryURL Package hosted pub repository URL.
     *
     * @throws IllegalPubPackageNamingException If package name is illegal.
     */
    public ThirdPartyHostedReference(@Nonnull String name, @Nonnull URL repositoryURL)
            throws IllegalPubPackageNamingException {
        this(name, repositoryURL, name);
    }

    /**
     * Get a URL of pub repository which using to grab this package.
     *
     * @return Location of pub repository.
     */
    @Nonnull
    public URL repositoryURL() {
        return repositoryURL;
    }

    /**
     * A package name on the hosted repository.
     *
     * @return A {@link String} of package name.
     */
    @Nonnull
    public String hostedName() {
        return hostedName;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public PubSemVerConstraint versionConstraint() {
        return versionConstraint;
    }

    /**
     * Change another repository URL to download this package.
     *
     * @param repositoryURL URL of pub repository.
     *
     * @return {@link ThirdPartyHostedReference} with applied URL.
     */
    @Nonnull
    public ThirdPartyHostedReference changeRepositoryURL(@Nonnull URL repositoryURL) {
        return DependencyReference.modifyHandler(
                () -> new ThirdPartyHostedReference(name(), repositoryURL, hostedName, versionConstraint)
        );
    }

    /**
     * Change another repository URL to download this package.
     *
     * @param repositoryURL URL of pub repository.
     *
     * @return {@link ThirdPartyHostedReference} with applied URL.
     *
     * @throws MalformedURLException If parsed URL is not a valid {@link URL#URL(String)}.
     */
    @Nonnull
    public ThirdPartyHostedReference changeRepositoryURL(@Nonnull String repositoryURL) throws MalformedURLException {
        return changeRepositoryURL(new URL(repositoryURL));
    }

    /**
     * Change the package name in remote pub repository.
     *
     * @param hostedName Name of the package in repository.
     *
     * @return A {@link ThirdPartyHostedReference} with applied host name.
     *
     * @throws IllegalPubPackageNamingException If host name is illegal.
     */
    @Nonnull
    public ThirdPartyHostedReference changeHostedName(@Nonnull String hostedName)
            throws IllegalPubPackageNamingException {
        return new ThirdPartyHostedReference(name(), repositoryURL, hostedName, versionConstraint);
    }

    /**
     * Change the host name to the same with {@link #name()}.
     *
     * @return Applied {@link ThirdPartyHostedReference} which {@link #hostedName()} is the same with {@link #name()}.
     */
    @Nonnull
    public ThirdPartyHostedReference changeHostedName() {
        return DependencyReference.modifyHandler(() -> changeHostedName(name()));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ThirdPartyHostedReference changeVersionConstraint(@Nonnull PubSemVerConstraint versionConstraint) {
        return DependencyReference.modifyHandler(
                () -> new ThirdPartyHostedReference(name(), repositoryURL, hostedName, versionConstraint)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThirdPartyHostedReference that = (ThirdPartyHostedReference) o;
        return name().equals(that.name())
                && repositoryURL.equals(that.repositoryURL)
                && hostedName.equals(that.hostedName)
                && versionConstraint.equals(that.versionConstraint);
    }

    /**
     * {@inheritDoc}
     *
     * @return Hashed {@link #repositoryURL()}, {@link #hostedName()} and {@link #versionConstraint()} with
     *         {@link DependencyReference#hashCode()} added.
     */
    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(repositoryURL, hostedName, versionConstraint);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String toString() {
        return "ThirdPartyHostedReference{" +
                "name='" + name() + '\'' +
                ", repositoryURL=" + repositoryURL +
                ", hostedName='" + hostedName + '\'' +
                ", versionConstraint=" + versionConstraint +
                '}';
    }
}
