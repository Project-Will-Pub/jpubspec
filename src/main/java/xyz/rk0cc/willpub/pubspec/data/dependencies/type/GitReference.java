package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import xyz.rk0cc.jogu.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public final class GitReference extends DependencyReference {
    private final GitRepositoryURL repositoryURL;
    private final String path, ref;

    public GitReference(
            @Nonnull String name,
            @Nonnull GitRepositoryURL repositoryURL,
            @Nullable String path,
            @Nullable String ref
    ) {
        super(name);
        this.repositoryURL = repositoryURL;
        this.path = path;
        this.ref = ref;
    }

    public GitReference(@Nonnull String name, @Nonnull GitRepositoryURL repositoryURL) {
        this(name, repositoryURL, null, null);
    }

    @Nonnull
    public GitRepositoryURL repositoryURL() {
        return repositoryURL;
    }

    @Nullable
    public String path() {
        return path;
    }

    @Nullable
    public String ref() {
        return ref;
    }

    @Nonnull
    public GitReference changeRepositoryURL(@Nonnull GitRepositoryURL repositoryURL) {
        return new GitReference(name(), repositoryURL, path, ref);
    }

    @Nonnull
    public GitReference changeRepositoryURL(@Nonnull String repositoryURL) {
        try {
            return new GitReference(name(), GitRepositoryURL.parse(repositoryURL), path, ref);
        } catch (UnknownGitRepositoryURLTypeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Nonnull
    public GitReference changePath(@Nullable String path) {
        return new GitReference(name(), repositoryURL, path, ref);
    }

    @Nonnull
    public GitReference changeRef(@Nullable String ref) {
        return new GitReference(name(), repositoryURL, path, ref);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitReference that = (GitReference) o;
        return name().equals(that.name())
                && repositoryURL.equals(that.repositoryURL)
                && Objects.equals(path, that.path)
                && Objects.equals(ref, that.ref);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), repositoryURL, path, ref);
    }

    @Nonnull
    @Override
    public String toString() {
        return "GitReference{" +
                "name=" + name() +
                ", repositoryURL=" + repositoryURL +
                ", path='" + path + '\'' +
                ", ref='" + ref + '\'' +
                '}';
    }
}
