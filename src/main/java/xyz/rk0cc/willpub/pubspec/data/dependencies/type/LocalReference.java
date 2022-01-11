package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubPackageNamingException;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.*;
import java.util.Objects;

public final class LocalReference extends DependencyReference {
    private final String path;

    public LocalReference(@Nonnull String name, @Nonnull String path) throws IllegalPubPackageNamingException {
        super(name);
        this.path = path;
    }

    @Nonnull
    public String path() {
        return path;
    }

    @Nonnull
    public LocalReference changePath(@Nonnull String path) {
        return DependencyReference.modifyHandler(() -> new LocalReference(name(), path));
    }

    @Nonnull
    public File toFile(@Nonnull Path projectPath) throws NotDirectoryException {
        final File f = projectPath.resolve(path).toFile();

        if (!f.isDirectory()) throw new NotDirectoryException(f.getAbsolutePath());

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
}
