package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

public sealed abstract class DependencyReference implements Serializable
        permits GitReference, HostedReference, LocalReference, SDKReference, ThirdPartyHostedReference {
    private final String name;

    DependencyReference(@Nonnull String name) {
        this.name = name;
    }

    public final String name() {
        return name;
    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
