package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubPackageNamingException;
import xyz.rk0cc.willpub.pubspec.PubspecValueValidator;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

public sealed abstract class DependencyReference implements Serializable
        permits GitReference, HostedReference, LocalReference, SDKReference, ThirdPartyHostedReference {
    private final String name;

    DependencyReference(@Nonnull String name) throws IllegalPubPackageNamingException {
        PubspecValueValidator.ValueAssertion.assertPackageNaming(name);
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

    static <D extends DependencyReference> D modifyHandler(@Nonnull DependencyModifyFunction<D> modifier) {
        try {
            return modifier.updated();
        } catch (IllegalPubPackageNamingException e) {
            throw new AssertionError("False positive illegal package name caught.", e);
        }
    }

    @FunctionalInterface
    interface DependencyModifyFunction<D extends DependencyReference> {
        D updated() throws IllegalPubPackageNamingException;
    }
}
