package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;
import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubPackageNamingException;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Reference the package is bundled with SDK.
 * <br/>
 * Normally, Dart does not bundle with any package during installation. It commonly refers to a platform which based on
 * Dart like Flutter which coming with additional package to support platform.
 *
 * @since 1.0.0
 */
public final class SDKReference extends DependencyReference
        implements VersionConstrainedDependency<SDKReference> {
    private final String sdk;
    private final PubSemVerConstraint versionConstraint;

    /**
     * Create new {@link SDKReference} which come from SDK itself.
     *
     * @param name Package name.
     * @param sdk Package SDK name.
     * @param versionConstraint Accepted version constraint range for this package.
     *
     * @throws IllegalPubPackageNamingException If the package name is illegal.
     */
    public SDKReference(@Nonnull String name, @Nonnull String sdk, @Nonnull PubSemVerConstraint versionConstraint)
            throws IllegalPubPackageNamingException {
        super(name);
        this.sdk = sdk;
        this.versionConstraint = versionConstraint;
    }

    /**
     * Create new {@link SDKReference} without version constraint which come from SDK itself.
     *
     * @param name Package name.
     * @param sdk Package SDK name.
     *
     * @throws IllegalPubPackageNamingException If the package name is illegal.
     */
    public SDKReference(@Nonnull String name, @Nonnull String sdk) throws IllegalPubPackageNamingException {
        this(name, sdk, PubSemVerConstraint.parse(null));
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
     * Get an SDK name which the package comes from.
     *
     * @return Name of SDK platform.
     */
    @Nonnull
    public String sdk() {
        return sdk;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public SDKReference changeVersionConstraint(@Nonnull PubSemVerConstraint versionConstraint) {
        return DependencyReference.modifyHandler(() -> new SDKReference(name(), sdk, versionConstraint));
    }

    /**
     * Change SDK to refer this package.
     *
     * @param sdk New SDK name.
     *
     * @return Applied {@link SDKReference} with the new SDK name.
     */
    @Nonnull
    public SDKReference changeSDK(@Nonnull String sdk) {
        return DependencyReference.modifyHandler(() -> new SDKReference(name(), sdk, versionConstraint));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SDKReference that = (SDKReference) o;
        return name().equals(that.name())
                && sdk.equals(that.sdk)
                && versionConstraint.equals(that.versionConstraint);
    }

    /**
     * {@inheritDoc}
     *
     * @return Hashed {@link #sdk()} and {@link #versionConstraint()} with {@link DependencyReference#hashCode()} added.
     */
    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(sdk, versionConstraint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "SDKReference{" +
                "name='" + name() + '\'' +
                " sdk='" + sdk + '\'' +
                ", versionConstraint=" + versionConstraint +
                '}';
    }
}