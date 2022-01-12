package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;
import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubPackageNamingException;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public final class ThirdPartyHostedReference extends DependencyReference implements VersionConstrainedDependency {
    public static boolean SUCCINCT_THIRD_PARTY_HOSTED_FORMAT = true;

    private final URL repositoryURL;
    private final String hostedName;
    private final PubSemVerConstraint versionConstraint;

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

    public ThirdPartyHostedReference(
            @Nonnull String name,
            @Nonnull URL repositoryURL,
            @Nonnull PubSemVerConstraint versionConstraint
    ) throws IllegalPubPackageNamingException {
        this(name, repositoryURL, name, versionConstraint);
    }

    public ThirdPartyHostedReference(@Nonnull String name, @Nonnull URL repositoryURL, @Nonnull String hostedName)
            throws IllegalPubPackageNamingException {
        this(name, repositoryURL, hostedName, PubSemVerConstraint.parse(null));
    }

    public ThirdPartyHostedReference(@Nonnull String name, @Nonnull URL repositoryURL)
            throws IllegalPubPackageNamingException {
        this(name, repositoryURL, name);
    }

    @Nonnull
    public URL repositoryURL() {
        return repositoryURL;
    }

    @Nonnull
    public String hostedName() {
        return hostedName;
    }

    @Nonnull
    @Override
    public PubSemVerConstraint versionConstraint() {
        return versionConstraint;
    }

    @Nonnull
    public ThirdPartyHostedReference changeRepositoryURL(@Nonnull URL repositoryURL) {
        return DependencyReference.modifyHandler(
                () -> new ThirdPartyHostedReference(name(), repositoryURL, hostedName, versionConstraint)
        );
    }

    @Nonnull
    public ThirdPartyHostedReference changeRepositoryURL(@Nonnull String repositoryURL) {
        try {
            return changeRepositoryURL(new URL(repositoryURL));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Nonnull
    public ThirdPartyHostedReference changeHostedName(@Nonnull String hostedName) {
        return DependencyReference.modifyHandler(
                () -> new ThirdPartyHostedReference(name(), repositoryURL, hostedName, versionConstraint)
        );
    }

    @Nonnull
    public ThirdPartyHostedReference changeHostedName() {
        return changeHostedName(name());
    }

    @Nonnull
    @Override
    public ThirdPartyHostedReference changeVersionConstraint(@Nonnull PubSemVerConstraint versionConstraint) {
        return DependencyReference.modifyHandler(
                () -> new ThirdPartyHostedReference(name(), repositoryURL, hostedName, versionConstraint)
        );
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), repositoryURL, hostedName, versionConstraint);
    }

    @Nonnull
    @Override
    public String toString() {
        return "ThirdPartyHostedReference{" +
                "name=" + name() +
                ", repositoryURL=" + repositoryURL +
                ", hostedName='" + hostedName + '\'' +
                ", versionConstraint=" + versionConstraint +
                '}';
    }
}
