package xyz.rk0cc.willpub.exceptions.pubspec;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Initializing {@link xyz.rk0cc.willpub.pubspec.PubspecManager#PubspecManager(Path)} with a {@link Path} of invalid
 * Dart project directory applied.
 *
 * @since 1.0.0
 */
public class ApplyNonPubProjectDirectoryException extends IOException implements PubspecException {
    /**
     * An invalid path applied during setup.
     */
    public final Path appliedPath;

    /**
     * Create new exception which the {@link Path path} is not a Dart project directory.
     *
     * @param appliedPath Invalid {@link Path} which no Dart thing related.
     */
    public ApplyNonPubProjectDirectoryException(@Nonnull Path appliedPath) {
        super("Applied directory is not a validated pub project file");
        assert !appliedPath.isAbsolute()
                || !appliedPath.toFile().isDirectory()
                || !appliedPath.resolve("pubspec.yaml").toFile().isFile();
        this.appliedPath = appliedPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCausedConfigurationMessage() {
        return "\n\nApplied path: " + appliedPath.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + getCausedConfigurationMessage();
    }
}
