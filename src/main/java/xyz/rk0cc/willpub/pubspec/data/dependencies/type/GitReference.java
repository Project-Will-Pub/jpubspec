package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import xyz.rk0cc.jogu.GitRepositoryURL;
import xyz.rk0cc.jogu.UnknownGitRepositoryURLTypeException;
import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubPackageNamingException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Indicating this dependency is referencing remote Git repository.
 * <br/>
 * The remote Git repository must be contained completed Dart project layout inside. If contains in non-root directory,
 * {@link #path()} must be provided.
 * <br/>
 * The dependency will be downloaded with the latest commit in default branch. If {@link #ref()} is applied, pub will
 * follow this table to referencing dependencies.
 * <table border="1">
 *     <tr>
 *         <th>Value of <code>ref</code></th>
 *         <th>Applied result</th>
 *     </tr>
 *     <tr>
 *         <td>Branch name</td>
 *         <td>Latest commit of this branch name</td>
 *     </tr>
 *     <tr>
 *         <td>Tag name</td>
 *         <td>A commit which refer to the tag name</td>
 *     </tr>
 *     <tr>
 *         <td>Commit hash</td>
 *         <td>A commit of specific hash</td>
 *     </tr>
 * </table>
 *
 * @since 1.0.0
 */
public final class GitReference extends DependencyReference {
    private final GitRepositoryURL repositoryURL;
    private final String path, ref;

    /**
     * Create new Git reference dependency data.
     *
     * @param name Dependency name.
     * @param repositoryURL URL of Git repository.
     * @param path Path to the package if not in root directory.
     * @param ref Git reference of getting package.
     *
     * @throws IllegalPubPackageNamingException If dependency name is not legal package name.
     */
    public GitReference(
            @Nonnull String name,
            @Nonnull GitRepositoryURL repositoryURL,
            @Nullable String path,
            @Nullable String ref
    ) throws IllegalPubPackageNamingException {
        super(name);
        this.repositoryURL = repositoryURL;
        this.path = path;
        this.ref = ref;
    }

    /**
     * Create new Git reference dependency data.
     *
     * @param name Dependency name.
     * @param repositoryURL URL of Git repository.
     *
     * @throws IllegalPubPackageNamingException If dependency name is not legal package name.
     */
    public GitReference(@Nonnull String name, @Nonnull GitRepositoryURL repositoryURL)
            throws IllegalPubPackageNamingException {
        this(name, repositoryURL, null, null);
    }

    /**
     * Get a URL which pointing to remote repository uses for this dependency.
     *
     * @return Git repository URL of this dependency.
     */
    @Nonnull
    public GitRepositoryURL repositoryURL() {
        return repositoryURL;
    }

    /**
     * The package directory location if the package is not in root directory.
     *
     * @return Path to package in Git repository, <code>null</code> if in root directory.
     */
    @Nullable
    public String path() {
        return path;
    }

    /**
     * Referencing which Git commit is used for getting dependency.
     *
     * @return Either commit hash, branch name, tag name or <code>null</code> if using the latest commit in default
     *         branch.
     */
    @Nullable
    public String ref() {
        return ref;
    }

    /**
     * Change the location of Git repository.
     *
     * @param repositoryURL New location of Git repository URL.
     *
     * @return New {@link GitReference} with applied repository URL.
     */
    @Nonnull
    public GitReference changeRepositoryURL(@Nonnull GitRepositoryURL repositoryURL) {
        return DependencyReference.modifyHandler(() -> new GitReference(name(), repositoryURL, path, ref));
    }

    /**
     * Change the location of Git repository.
     *
     * @param repositoryURL New location of Git repository URL.
     *
     * @return New {@link GitReference} with applied repository URL.
     *
     * @throws UnknownGitRepositoryURLTypeException If provided URL is not a valid Git repository URL.
     */
    @Nonnull
    public GitReference changeRepositoryURL(@Nonnull String repositoryURL) throws UnknownGitRepositoryURLTypeException {
        return changeRepositoryURL(GitRepositoryURL.parse(repositoryURL));
    }

    /**
     * Change new path to the package in the repository.
     *
     * @param path Path to package in Git repository, <code>null</code> if not applied.
     *
     * @return New {@link GitReference} with assigned path.
     */
    @Nonnull
    public GitReference changePath(@Nullable String path) {
        return DependencyReference.modifyHandler(() -> new GitReference(name(), repositoryURL, path, ref));
    }

    /**
     * Change reference to get the package in the repository.
     *
     * @param ref Reference for getting package in Git repository.
     *
     * @return New {@link GitReference} with assigned reference.
     */
    @Nonnull
    public GitReference changeRef(@Nullable String ref) {
        return DependencyReference.modifyHandler(() -> new GitReference(name(), repositoryURL, path, ref));
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     *
     * @return Hashed {@link #repositoryURL()}, {@link #path()} and {@link #ref()} with origin
     * {@link DependencyReference#hashCode()} added.
     */
    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(repositoryURL, path, ref);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String toString() {
        return "GitReference{" +
                "name='" + name() + '\'' +
                ", repositoryURL=" + repositoryURL +
                ", path='" + path + '\'' +
                ", ref='" + ref + '\'' +
                '}';
    }
}
