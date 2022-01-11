package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;
import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubPackageNamingException;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class SDKReference extends DependencyReference implements VersionConstrainedDependency {
    private final String sdk;
    private final PubSemVerConstraint versionConstraint;

    public SDKReference(@Nonnull String name, @Nonnull String sdk, @Nonnull PubSemVerConstraint versionConstraint)
            throws IllegalPubPackageNamingException {
        super(name);
        this.sdk = sdk;
        this.versionConstraint = versionConstraint;
    }

    public SDKReference(@Nonnull String name, @Nonnull String sdk) throws IllegalPubPackageNamingException {
        this(name, sdk, PubSemVerConstraint.parse(null));
    }

    @Nonnull
    @Override
    public PubSemVerConstraint versionConstraint() {
        return versionConstraint;
    }

    @Nonnull
    public String sdk() {
        return sdk;
    }

    @Nonnull
    @Override
    public SDKReference changeVersionConstraint(@Nonnull PubSemVerConstraint versionConstraint) {
        return DependencyReference.modifyHandler(() -> new SDKReference(name(), sdk, versionConstraint));
    }

    @Nonnull
    public SDKReference changeSDK(@Nonnull String sdk) {
        return DependencyReference.modifyHandler(() -> new SDKReference(name(), sdk, versionConstraint));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SDKReference that = (SDKReference) o;
        return name().equals(that.name())
                && sdk.equals(that.sdk)
                && versionConstraint.equals(that.versionConstraint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sdk, versionConstraint);
    }
}