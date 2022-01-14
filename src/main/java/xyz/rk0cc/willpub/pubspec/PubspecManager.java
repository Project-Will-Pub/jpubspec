package xyz.rk0cc.willpub.pubspec;

import xyz.rk0cc.willpub.exceptions.pubspec.ApplyNonPubProjectDirectoryException;
import xyz.rk0cc.willpub.pubspec.data.Pubspec;
import xyz.rk0cc.willpub.pubspec.data.PubspecSnapshot;
import xyz.rk0cc.willpub.pubspec.parser.PubspecParser;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class PubspecManager {
    private final Path projectPath;
    private final PubspecArchiver archiver;

    public PubspecManager(@Nonnull Path projectPath) throws ApplyNonPubProjectDirectoryException {
        if (!projectPath.isAbsolute()
                || !projectPath.toFile().isDirectory()
                || !projectPath.resolve("pubspec.yaml").toFile().isFile()
        ) throw new ApplyNonPubProjectDirectoryException(projectPath);

        this.projectPath = projectPath;
        this.archiver = new PubspecArchiver(projectPath);
    }

    @Nonnull
    public final File pubspecYAML() {
        return projectPath.resolve("pubspec.yaml").toFile();
    }

    @Nonnull
    public final PubspecArchiver archiver() {
        assert archiver.projectPath().equals(projectPath);
        return archiver;
    }

    public final Pubspec loadPubspec() throws IOException {
        return PubspecParser.PUBSPEC_MAPPER.readValue(pubspecYAML(), Pubspec.class);
    }

    public final void savePubspec(@Nonnull Pubspec pubspec) throws IOException {
        PubspecParser.PUBSPEC_MAPPER.writeValue(pubspecYAML(), pubspec);
    }

    public final void savePubspecFromLatestArchive() throws IOException {
        savePubspec(PubspecSnapshot.getMutableFromSnapshot(archiver.recentSnapshot()));
    }

}
