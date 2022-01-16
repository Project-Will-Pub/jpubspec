package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;
import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubPackageNamingException;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Referencing this dependency from default pub repository server with given {@link PubSemVerConstraint}.
 * <br/>
 * The dependencies will be grabbed on environment variable <code>PUB_HOSTED_URL</code> which is
 * <a href="https://pub.dev"><code>pub.dev</code></a> by default.
 *
 * @since 1.0.0
 *
 * @see ThirdPartyHostedReference
 */
public final class HostedReference extends DependencyReference
        implements VersionConstrainedDependency<HostedReference> {
    private final PubSemVerConstraint versionConstraint;

    /**
     * Create new dependency which grab from pub hosting URL.
     *
     * @param name Package name.
     * @param versionConstraint Version constraint of import package.
     *
     * @throws IllegalPubPackageNamingException If package name is illegal.
     */
    public HostedReference(@Nonnull String name, @Nonnull PubSemVerConstraint versionConstraint)
            throws IllegalPubPackageNamingException {
        super(name);
        this.versionConstraint = versionConstraint;
    }

    /**
     * Create new dependency which grab from pub hosting URL and applied as
     * {@link xyz.rk0cc.josev.constraint.pub.PubConstraintPattern#ANY}
     *
     * @param name Package name.
     *
     * @throws IllegalPubPackageNamingException If package name is illegal.
     */
    public HostedReference(@Nonnull String name) throws IllegalPubPackageNamingException {
        this(name, PubSemVerConstraint.parse(null));
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
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public HostedReference changeVersionConstraint(@Nonnull PubSemVerConstraint versionConstraint) {
        return DependencyReference.modifyHandler(() -> new HostedReference(name(), versionConstraint));
    }

    /**
     * Convert it to a {@link ThirdPartyHostedReference} with declared {@link ThirdPartyHostedReference#repositoryURL()}
     * according to <code>PUB_HOSTED_URL</code>.
     *
     * @return A {@link ThirdPartyHostedReference} with declared hosted URL from  <code>PUB_HOSTED_URL</code>.
     */
    public ThirdPartyHostedReference convertItAsThirdParty() {
        URL pubRepo;

        try {
            String phu = System.getenv("PUB_HOSTED_URL");

            // It uses pub.dev if environment is not applied
            pubRepo = new URL(phu == null ? "https://pub.dev" : phu);
        } catch (MalformedURLException e) {
            throw new RuntimeException("PUB_HOSTED_URL is applied but not a URL", e);
        }

        return DependencyReference.modifyHandler(
                () -> new ThirdPartyHostedReference(name(), pubRepo, versionConstraint)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HostedReference that = (HostedReference) o;
        return name().equals(that.name()) && Objects.equals(versionConstraint, that.versionConstraint);
    }

    /**
     * {@inheritDoc}
     *
     * @return Hashed {@link #versionConstraint()} with {@link DependencyReference#hashCode()} added.
     */
    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(versionConstraint);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String toString() {
        return "HostedReference{" +
                "name='" + name() + '\'' +
                ", versionConstraint=" + versionConstraint +
                '}';
    }
}
