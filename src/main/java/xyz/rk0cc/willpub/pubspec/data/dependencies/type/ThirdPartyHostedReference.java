package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public final class ThirdPartyHostedReference extends DependencyReference {
    private final URL repositoryURL;
    private final String hostedName;
    private final PubSemVerConstraint versionConstraint;

    public ThirdPartyHostedReference(
            @Nonnull String name,
            @Nonnull URL repositoryURL,
            @Nonnull String hostedName,
            @Nonnull PubSemVerConstraint versionConstraint
    ) {
        super(name);
        this.repositoryURL = repositoryURL;
        this.hostedName = hostedName;
        this.versionConstraint = versionConstraint;
    }

    public ThirdPartyHostedReference(
            @Nonnull String name,
            @Nonnull URL repositoryURL,
            @Nonnull PubSemVerConstraint versionConstraint
    ) {
        this(name, repositoryURL, name, versionConstraint);
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
    public PubSemVerConstraint versionConstraint() {
        return versionConstraint;
    }

    @Nonnull
    public ThirdPartyHostedReference changeRepositoryURL(@Nonnull URL repositoryURL) {
        return new ThirdPartyHostedReference(name(), repositoryURL, hostedName, versionConstraint);
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
        return new ThirdPartyHostedReference(name(), repositoryURL, hostedName, versionConstraint);
    }

    @Nonnull
    public ThirdPartyHostedReference changeHostedName() {
        return changeHostedName(name());
    }

    @Nonnull
    public ThirdPartyHostedReference changeVersionConstraint(@Nonnull PubSemVerConstraint versionConstraint) {
        return new ThirdPartyHostedReference(name(), repositoryURL, hostedName, versionConstraint);
    }

    @Nonnull
    public ThirdPartyHostedReference changeVersionConstraint(@Nullable String versionConstraint) {
        return changeVersionConstraint(PubSemVerConstraint.parse(versionConstraint));
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
