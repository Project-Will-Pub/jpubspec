package xyz.rk0cc.willpub.exceptions.pubspec;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;

public class ApplyNonPubProjectDirectoryException extends IOException implements PubspecException {
    public final Path appliedPath;

    public ApplyNonPubProjectDirectoryException(@Nonnull Path appliedPath) {
        super("Applied directory is not a validated pub project file");
        assert !appliedPath.isAbsolute()
                || !appliedPath.toFile().isDirectory()
                || !appliedPath.resolve("pubspec.yaml").toFile().isFile();
        this.appliedPath = appliedPath;
    }

    @Override
    public String getCausedConfigurationMessage() {
        return "\n\nApplied path: " + appliedPath.toString();
    }

    @Override
    public String toString() {
        return super.toString() + getCausedConfigurationMessage();
    }
}
