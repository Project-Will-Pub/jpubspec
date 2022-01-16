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
     * Get a {@link String} of the path which located to the dependency package.
     *
     * @return Path to the dependency.
     */
    @Nonnull
    public Path path() {
        return path;
    }

    /**
     * Change the location to
     *
     * @param path
     *
     * @return
     */
    @Nonnull
    public LocalReference changePath(@Nonnull Path path) {
        return DependencyReference.modifyHandler(() -> new LocalReference(name(), path));
    }

    @Nonnull
    public File toFile(@Nonnull Path projectPath) throws NotDirectoryException {
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

    @Nonnull
    public File toFile(@Nonnull String projectPath) throws NotDirectoryException {
        return toFile(Paths.get(projectPath));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalReference that = (LocalReference) o;
        return name().equals(that.name()) && path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path);
    }

    @Override
    public String toString() {
        return "LocalReference{" +
                "name='" + name() + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
