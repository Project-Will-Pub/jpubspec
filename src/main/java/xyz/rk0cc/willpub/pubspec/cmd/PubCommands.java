package xyz.rk0cc.willpub.pubspec.cmd;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public abstract class PubCommands {
    private final Path currentWorkingDirectory;
    private final boolean flutterProject;

    protected PubCommands(@Nonnull Path currentWorkingDirectory, boolean flutterProject) {
        assert currentWorkingDirectory.isAbsolute();
        assert currentWorkingDirectory.toFile().isDirectory();
        this.currentWorkingDirectory = currentWorkingDirectory;
        this.flutterProject = flutterProject;
    }


}
