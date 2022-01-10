package xyz.rk0cc.willpub.pubspec.data.dependencies;

import xyz.rk0cc.willpub.pubspec.data.dependencies.type.DependencyReference;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class DependenciesReferenceSet implements Set<DependencyReference>, Serializable, Cloneable {
    private final HashMap<String, DependencyReference> references;

    public DependenciesReferenceSet() {
        this.references = new HashMap<>();
    }

    public DependenciesReferenceSet(@Nonnull DependenciesReferenceSet references) {
        this.references = new HashMap<>(references.references);
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

    final boolean $add(@Nonnull DependencyReference dependencyReferenceType) {
        return references.putIfAbsent(dependencyReferenceType.name(), dependencyReferenceType) != null;
    }

    @Override
    public final boolean remove(@Nonnull Object o) {
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
    public boolean addAll(@Nonnull Collection<? extends DependencyReference> c) {
        try {
            c.forEach(this::add);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {
        return false;
    }

    @Override
    public boolean removeAll(@Nonnull Collection<?> c) {
        return false;
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
    public Stream<DependencyReference> parallelStream() {
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
