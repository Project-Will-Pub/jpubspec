package xyz.rk0cc.willpub.pubspec.data.dependencies;

import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubPackageNamingException;
import xyz.rk0cc.willpub.pubspec.data.dependencies.type.DependencyReference;
import xyz.rk0cc.willpub.pubspec.data.PubspecValueValidator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public sealed abstract class DependenciesReferenceSet implements Set<DependencyReference>, Serializable, Cloneable
        permits ImportedReferenceSet, OverrideReferenceSet {
    private final HashMap<String, DependencyReference> references;
    private final boolean unmodifiable;

    DependenciesReferenceSet() {
        this.references = new HashMap<>();
        this.unmodifiable = false;
    }

    DependenciesReferenceSet(@Nonnull DependenciesReferenceSet references, boolean unmodifiable) {
        this.references = new HashMap<>(references.references);
        this.unmodifiable = unmodifiable;
    }

    DependenciesReferenceSet(@Nonnull DependenciesReferenceSet references) {
        this(references, false);
    }

    public final boolean isUnmodifiable() {
        return unmodifiable;
    }

    private void assertModifiable() {
        if (unmodifiable) throw new UnsupportedOperationException("Unmodifiable mode enabled");
    }

    @Nonnegative
    @Override
    public final int size() {
        return references.size();
    }

    @Override
    public final boolean isEmpty() {
        return references.isEmpty();
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public final boolean contains(@Nonnull Object o) {
        return o instanceof String s ? references.containsKey(s) : references.containsValue(o);
    }

    @Nonnull
    @Override
    public final Iterator<DependencyReference> iterator() {
        return toNativeSet().iterator();
    }

    @Nonnull
    @Override
    public final Object[] toArray() {
        return toNativeSet().toArray();
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    @Nonnull
    @Override
    public final <T> T[] toArray(@Nonnull T[] a) {
        return toNativeSet().toArray(a);
    }

    abstract boolean isAllowToAdd(@Nonnull DependencyReference dependencyReference);

    private boolean mockAddPassed(@Nonnull DependencyReference dependencyReference) {
        if (!isAllowToAdd(dependencyReference)) return false;

        HashMap<String, DependencyReference> dummy = new HashMap<>();

        return dummy.put(dependencyReference.name(), dependencyReference) != null;
    }

    @Override
    public final boolean add(@Nonnull DependencyReference dependencyReference) {
        assertModifiable();

        return mockAddPassed(dependencyReference)
                && references.putIfAbsent(dependencyReference.name(), dependencyReference) != null;
    }

    public final boolean set(@Nonnull DependencyReference dependencyReference) {
        assertModifiable();

        return mockAddPassed(dependencyReference)
                && references.put(dependencyReference.name(), dependencyReference) != null;
    }

    @Override
    public final boolean remove(@Nonnull Object o) {
        assertModifiable();

        if (o instanceof DependencyReference drt) return references.remove(drt.name(), drt);
        else if (o instanceof String s) return references.remove(s) != null;
        else throw new ClassCastException("'" + o.getClass().getName() + "' can not remove reference in this set");
    }

    @Override
    public final boolean containsAll(@Nonnull Collection<?> c) {
        return c.stream().allMatch(ct -> {
            final Stream<DependencyReference> streamType = toNativeSet().stream();

            if (ct instanceof String s)
                return streamType.allMatch(drts -> drts.name().equals(s));
            else
                return streamType.allMatch(ct::equals);
        });
    }

    @Override
    public final boolean addAll(@Nonnull Collection<? extends DependencyReference> c) {
        throw new UnsupportedOperationException("This set can not use add all functions");
    }

    @Override
    public final boolean retainAll(@Nonnull Collection<?> c) {
        throw new UnsupportedOperationException("This set can not use retain all functions");
    }

    @Override
    public final boolean removeAll(@Nonnull Collection<?> c) {
        throw new UnsupportedOperationException("This set can not use remove all functions");
    }

    @Nonnull
    public final DependencyReference get(@Nonnull String dependencyName) throws IllegalPubPackageNamingException {
        PubspecValueValidator.ValueAssertion.assertPackageNaming(dependencyName);
        return Objects.requireNonNull(references.get(dependencyName));
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public final <D extends DependencyReference> D get(
            @Nonnull String dependencyName,
            @Nonnull Class<D> dependencyType
    ) throws IllegalPubPackageNamingException {
        if (Modifier.isAbstract(dependencyType.getModifiers()))
            throw new IllegalArgumentException("Do not uses abstracted dependency type to apply.");

        final DependencyReference initGet = get(dependencyName);

        if (!initGet.getClass().equals(dependencyType))
            throw new ClassCastException(
                    "Dependency '" + dependencyName + "' is " + initGet.getClass().getName() +
                    ", not " + dependencyType.getName()
            );

        return (D) initGet;
    }

    @Override
    public final void clear() {
        references.clear();
    }

    @Override
    public final Spliterator<DependencyReference> spliterator() {
        return toNativeSet().spliterator();
    }

    @Override
    public final Stream<DependencyReference> stream() {
        return toNativeSet().stream();
    }

    @Override
    public final Stream<DependencyReference> parallelStream() {
        return toNativeSet().parallelStream();
    }

    @Override
    public final void forEach(@Nonnull Consumer<? super DependencyReference> action) {
        toNativeSet().forEach(action);
    }

    @Nonnull
    public final Set<DependencyReference> toNativeSet() {
        return new HashSet<>(references.values());
    }

    @Override
    public abstract DependenciesReferenceSet clone();
}
