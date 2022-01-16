package xyz.rk0cc.willpub.pubspec;

import xyz.rk0cc.willpub.exceptions.pubspec.ApplyNonPubProjectDirectoryException;
import xyz.rk0cc.willpub.pubspec.data.Pubspec;
import xyz.rk0cc.willpub.pubspec.data.PubspecSnapshot;
import xyz.rk0cc.willpub.pubspec.data.dependencies.type.LocalReference;
import xyz.rk0cc.willpub.pubspec.parser.PubspecParser;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;

/**
 * Manager of single <code>pubspec.yaml</code> with giving project {@link Path}.
 * <br/>
 * Each {@link PubspecManager} representing a single {@link Path} of the Dart project with can not mix uses.
 *
 * @since 1.0.0
 */
public class PubspecManager {
    private final Path projectPath;
    private final PubspecArchiver archiver;

    /**
     * Create new instance of {@link PubspecManager} with given project {@link Path}.
     *
     * @param projectPath A {@link Path} of directory which contains <code>pubspec.yaml</code>.
     *
     * @throws ApplyNonPubProjectDirectoryException If applied <code>projectPath</code> return <code>false</code> for
     *                                              {@link File#isDirectory()} and {@link Path#isAbsolute()}.
     */
    public PubspecManager(@Nonnull Path projectPath) throws ApplyNonPubProjectDirectoryException {
        if (!projectPath.isAbsolute() || !projectPath.toFile().isDirectory())
            throw new ApplyNonPubProjectDirectoryException(projectPath);

        this.projectPath = projectPath;
        this.archiver = new PubspecArchiver(projectPath);
    }

    /**
     * Resolve <code>pubspec.yaml</code> as Java object {@link File}.
     *
     * @return A {@link File} of <code>pubspec.yaml</code> which resolve from project path.
     */
    @Nonnull
    public final File pubspecYAML() {
        return projectPath.resolve("pubspec.yaml").toFile();
    }

    /**
     * Get an archive manager for each {@link Pubspec}'s edit.
     *
     * @return {@link PubspecArchiver} with {@link PubspecSnapshot archived Pubspec} data.
     */
    @Nonnull
    public final PubspecArchiver archiver() {
        assert archiver.projectPath().equals(projectPath);
        return archiver;
    }

    /**
     * Read <code>pubspec.yaml</code> in current directory and convert to {@link Pubspec} for editing in Java.
     *
     * @return {@link Pubspec} context.
     *
     * @throws IOException If problem encountered during
     *                     {@link com.fasterxml.jackson.databind.ObjectMapper#readValue(File, Class)}.
     */
    @Nonnull
    public final Pubspec loadPubspec() throws IOException {
        return PubspecParser.PUBSPEC_MAPPER.readValue(pubspecYAML(), Pubspec.class);
    }

    /**
     * Write {@link Pubspec} to a file.
     *
     * @param pubspec A modified {@link Pubspec}
     *
     * @throws IOException If converting {@link Pubspec} to <code>pubspec.yaml</code> failed.
     */
    public final void savePubspec(@Nonnull Pubspec pubspec) throws IOException {
        PubspecParser.PUBSPEC_MAPPER.writeValue(pubspecYAML(), pubspec);
    }

    /**
     * Write <code>pubspec.yaml</code> using the latest {@link PubspecArchiver#archivePubspec(Pubspec) archived} version
     * of {@link Pubspec}.
     *
     * @throws IOException If converting {@link Pubspec} to <code>pubspec.yaml</code> failed.
     */
    public final void savePubspecFromLatestArchive() throws IOException {
        savePubspec(PubspecSnapshot.getMutableFromSnapshot(archiver.recentSnapshot()));
    }

    /**
     * Resolve {@link LocalReference#path()} to actual {@link File} object.
     *
     * @param localReference A reference that want to get a {@link File}.
     *
     * @return A {@link File} with resolved path from current project directory.
     */
    @Nonnull
    public final File resolvePathOfLocalReference(@Nonnull LocalReference localReference) {
        try {
            return localReference.toFile(projectPath);
        } catch (NotDirectoryException e) {
            throw new AssertionError("The project path is no longer as a directory unexpectedly", e);
        }
    }
}
