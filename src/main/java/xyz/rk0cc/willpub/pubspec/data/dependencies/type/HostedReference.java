package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class HostedReference extends DependencyReference implements VersionConstrainedDependency {
    private final PubSemVerConstraint versionConstraint;

    public HostedReference(@Nonnull String name, @Nonnull PubSemVerConstraint versionConstraint) {
        super(name);
        this.versionConstraint = versionConstraint;
    }

    public HostedReference(@Nonnull String name) {
        this(name, PubSemVerConstraint.parse(null));
    }

    @Nonnull
    @Override
    public PubSemVerConstraint versionConstraint() {
        return versionConstraint;
    }

    @Nonnull
    @Override
    public HostedReference changeVersionConstraint(@Nonnull PubSemVerConstraint versionConstraint) {
        return new HostedReference(name(), versionConstraint);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HostedReference that = (HostedReference) o;
        return name().equals(that.name()) && Objects.equals(versionConstraint, that.versionConstraint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), versionConstraint);
    }

    @Nonnull
    @Override
    public String toString() {
        return "HostedReference{" +
                "name=" + name() +
                ", versionConstraint=" + versionConstraint +
                '}';
    }
}
