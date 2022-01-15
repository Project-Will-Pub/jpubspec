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

/**
 * Contains multiple {@link DependencyReference} as a {@link Set}.
 * <br/>
 * It embedded {@link LinkedHashMap} to allows more convenience way to get {@link DependencyReference} which {@link Set}
 * can not.
 *
 * @since 1.0.0
 */
public sealed abstract class DependenciesReferenceSet implements Set<DependencyReference>, Serializable, Cloneable
        permits ImportedReferenceSet, OverrideReferenceSet {
    private final LinkedHashMap<String, DependencyReference> references;
    private final boolean unmodifiable;

    /**
     * Create new empty set.
     */
    DependenciesReferenceSet() {
        this.references = new LinkedHashMap<>();
        this.unmodifiable = false;
    }

    /**
     * Create new set with existed reference and allows modification or not.
     *
     * @param references Original references.
     * @param unmodifiable Forbid any modification in this object.
     */
    DependenciesReferenceSet(@Nonnull DependenciesReferenceSet references, boolean unmodifiable) {
        this.references = new LinkedHashMap<>(references.references);
        this.unmodifiable = unmodifiable;
    }

    /**
     * Create new modifiable set with existed reference.
     *
     * @param references Original reference.
     */
    DependenciesReferenceSet(@Nonnull DependenciesReferenceSet references) {
        this(references, false);
    }

    /**
     * Determine it disallow edit new {@link DependencyReference} or not.
     * <br/>
     * Despite it provided {@link Collections#unmodifiableSet(Set)} to mark unmodifiable. It missed all methods which
     * exclusive in {@link DependenciesReferenceSet} like {@link #set(DependencyReference)} and {@link #get(String)}.
     *
     * @return <code>true</code> if disallowed.
     */
    public final boolean isUnmodifiable() {
        return unmodifiable;
    }

    /**
     * An assertion method to prevent applying modification if {@link #isUnmodifiable()} return <code>true</code>.
     */
    private void assertModifiable() {
        if (unmodifiable) throw new UnsupportedOperationException("Unmodifiable mode enabled");
    }

    /**
     * {@inheritDoc}
     */
    @Nonnegative
    @Override
    public final int size() {
        return references.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isEmpty() {
        return references.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public final boolean contains(@Nonnull Object o) {
        return o instanceof String s ? references.containsKey(s) : references.containsValue(o);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public final Iterator<DependencyReference> iterator() {
        return toNativeSet().iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public final Object[] toArray() {
        return toNativeSet().toArray();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("SuspiciousToArrayCall")
    @Nonnull
    @Override
    public final <T> T[] toArray(@Nonnull T[] a) {
        return toNativeSet().toArray(a);
    }

    /**
     * To determine allowing incoming {@link DependencyReference} can be inserted.
     *
     * @param dependencyReference {@link DependencyReference} which pending to apply.
     *
     * @return <code>true</code> if allows.
     */
    abstract boolean isAllowToAdd(@Nonnull DependencyReference dependencyReference);

    /**
     * Mock {@link #add(DependencyReference)} operation to determine the reference can be appended.
     *
     * @param dependencyReference {@link DependencyReference} which pending to apply.
     *
     * @return <code>true</code> if allows.
     */
    private boolean mockAddPassed(@Nonnull DependencyReference dependencyReference) {
        if (!isAllowToAdd(dependencyReference)) return false;

        HashMap<String, DependencyReference> dummy = new HashMap<>();

        return dummy.put(dependencyReference.name(), dependencyReference) != null;
    }

    /**
     * Append the reference to this set.
     *
     * @param dependencyReference A {@link DependencyReference} which going to apply if no
     * {@link DependencyReference#name() same reference name} is applied already.
     *
     * @return <code>true</code> if appended.
     *
     * @throws UnsupportedOperationException If {@link #isUnmodifiable()} returns <code>true</code>.
     */
    @Override
    public final boolean add(@Nonnull DependencyReference dependencyReference) {
        assertModifiable();

        return mockAddPassed(dependencyReference)
                && references.putIfAbsent(dependencyReference.name(), dependencyReference) != null;
    }

    /**
     * Set the reference to this set.
     *
     * @param dependencyReference A {@link DependencyReference} which going to apply or modify is
     *                            {@link DependencyReference#name() dependencies name} existed already.
     *
     * @return <code>true</code> if set.
     *
     * @throws UnsupportedOperationException If {@link #isUnmodifiable()} returns <code>true</code>.
     */
    public final boolean set(@Nonnull DependencyReference dependencyReference) {
        assertModifiable();

        return mockAddPassed(dependencyReference)
                && references.put(dependencyReference.name(), dependencyReference) != null;
    }

    /**
     * Remove {@link DependencyReference} with given {@link Object}, it can be either {@link DependencyReference}
     * or {@link String}.
     *
     * @param o An object of {@link DependencyReference} or a {@link String} of package name is going to remove on this
     *          list.
     *
     * @return <code>true</code> if removed.
     *
     * @throws UnsupportedOperationException If {@link #isUnmodifiable()} returns <code>true</code>.
     */
    @Override
    public final boolean remove(@Nonnull Object o) {
        assertModifiable();

        if (o instanceof DependencyReference drt) return references.remove(drt.name(), drt);
        else if (o instanceof String s) return references.remove(s) != null;
        else throw new ClassCastException("'" + o.getClass().getName() + "' can not remove reference in this set");
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * <h2>{@linkplain DependenciesReferenceSet} does not support it.</h2>
     *
     * {@inheritDoc}
     */
    @Override
    public final boolean addAll(@Nonnull Collection<? extends DependencyReference> c) {
        throw new UnsupportedOperationException("This set can not use add all functions");
    }

    /**
     * <h2>{@linkplain DependenciesReferenceSet} does not support it.</h2>
     *
     * {@inheritDoc}
     */
    @Override
    public final boolean retainAll(@Nonnull Collection<?> c) {
        throw new UnsupportedOperationException("This set can not use retain all functions");
    }

    /**
     * <h2>{@linkplain DependenciesReferenceSet} does not support it.</h2>
     *
     * {@inheritDoc}
     */
    @Override
    public final boolean removeAll(@Nonnull Collection<?> c) {
        throw new UnsupportedOperationException("This set can not use remove all functions");
    }

    /**
     * Giving a package name to find related {@link DependencyReference}.
     *
     * @param dependencyName Dependency's name.
     *
     * @return Corresponded {@link DependencyReference} with given name.
     *
     * @throws IllegalPubPackageNamingException If the dependency name is not a valid name.
     * @throws NullPointerException If this dependency name does not define yet.
     */
    @Nonnull
    public final DependencyReference get(@Nonnull String dependencyName) throws IllegalPubPackageNamingException {
        PubspecValueValidator.ValueAssertion.assertPackageNaming(dependencyName);
        return Objects.requireNonNull(references.get(dependencyName));
    }

    /**
     * Giving a package name and return specific type of {@link DependencyReference}.
     *
     * @param dependencyName Dependency's name.
     * @param dependencyType {@link Class} which preferred return type of {@link DependencyReference}.
     * @param <D> Return type of {@link DependencyReference}.
     *
     * @return Corresponded {@link DependencyReference} with given name.
     *
     * @throws IllegalPubPackageNamingException If the dependency name is not a valid name.
     * @throws NullPointerException If this dependency name does not define yet.
     * @throws ClassCastException If actual type of {@link DependencyReference} is difference from
     *                            <code>dependencyType</code>.
     */
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

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException If {@link #isUnmodifiable()} returns <code>true</code>.
     */
    @Override
    public final void clear() {
        assertModifiable();

        references.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Spliterator<DependencyReference> spliterator() {
        return toNativeSet().spliterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Stream<DependencyReference> stream() {
        return toNativeSet().stream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Stream<DependencyReference> parallelStream() {
        return toNativeSet().parallelStream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void forEach(@Nonnull Consumer<? super DependencyReference> action) {
        toNativeSet().forEach(action);
    }

    /**
     * Convert it to a native {@link Set} which missing {@link #set(DependencyReference)} and {@link #get(String)}
     * features.
     *
     * @return {@link Set} with limited feature.
     */
    @Nonnull
    public final Set<DependencyReference> toNativeSet() {
        return new LinkedHashSet<>(references.values());
    }

    /**
     * Clone a new {@link DependenciesReferenceSet} with exact same data.
     *
     * @return New {@link DependenciesReferenceSet} with same data.
     */
    @Override
    public abstract DependenciesReferenceSet clone();
}
