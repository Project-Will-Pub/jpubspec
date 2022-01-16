package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubPackageNamingException;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.*;
import java.util.Objects;

/**
 * Getting dependency in local drive only.
 * <br/>
 * <h3>WARNING</h3>
 * The provided {@link #path()} is according to the working device's storage space only which it possibility absent
 * on other device.
 *
 * @since 1.0.0
 */
public final class LocalReference extends DependencyReference {
    private final Path path;

    /**
     * Create new {@link LocalReference} dependency info.
     *
     * @param name Package name.
     * @param path Path to the package (in local device).
     *
     * @throws IllegalPubPackageNamingException When the package name is illegal.
     */
    public LocalReference(@Nonnull String name, @Nonnull Path path) throws IllegalPubPackageNamingException {
        super(name);
        this.path = path;
    }

    /**
     * Get a {@link Path} which located to the dependency package.
     *
     * @return Path to the dependency.
     */
    @Nonnull
    public Path path() {
        return path;
    }

    /**
     * Change the location to related path.
     *
     * @param path New {@link Path} of package location.
     *
     * @return A {@link LocalReference} with new path applied.
     */
    @Nonnull
    public LocalReference changePath(@Nonnull Path path) {
        return DependencyReference.modifyHandler(() -> new LocalReference(name(), path));
    }

    /**
     * Change the location to related path.
     *
     * @param path Path to the package location or first path location.
     * @param subpath If <code>path</code> is not a completed path, it will be used to join as a complete path.
     *
     * @return A {@link LocalReference} with new path applied.
     */
    @Nonnull
    public LocalReference changePath(@Nonnull String path, String... subpath) {
        return changePath(Paths.get(path, subpath));
    }

    /**
     * Resolve an actual {@link File} object of referencing dependency's directory.
     *
     * @param projectPath An absolute path of the project directory.
     *
     * @return A {@link File} which is a directory of dependency project.
     *
     * @throws NotDirectoryException If {@link File#isDirectory()} return <code>false</code> which means the applied
     *                               path is not a directory.
     */
    @Nonnull
    public File toFile(@Nonnull Path projectPath) throws NotDirectoryException {
        assert projectPath.isAbsolute();

        final File f = projectPath.resolve(path).toFile();

        if (!f.isDirectory())
            throw new NotDirectoryException(f.getAbsolutePath()) {
                @Override
                public String getMessage() {
                    return "Local reference dependency path must be existed directory";
                }
            };

        return f;
    }

    /**
     * Resolve an actual {@link File} object of referencing dependency's directory.
     *
     * @param projectPath An absolute path of the project directory.
     *
     * @return A {@link File} which is a directory of dependency project.
     *
     * @throws NotDirectoryException If {@link File#isDirectory()} return <code>false</code> which means the applied
     *                               path is not a directory.
     */
    @Nonnull
    public File toFile(@Nonnull String projectPath) throws NotDirectoryException {
        return toFile(Paths.get(projectPath));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalReference that = (LocalReference) o;
        return name().equals(that.name()) && path.equals(that.path);
    }

    /**
     * {@inheritDoc}
     *
     * @return Hashed {@link #path()} with {@link DependencyReference#hashCode()} added.
     */
    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "LocalReference{" +
                "name='" + name() + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
