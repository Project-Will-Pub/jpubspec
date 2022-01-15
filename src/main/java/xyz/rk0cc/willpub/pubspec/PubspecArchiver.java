package xyz.rk0cc.willpub.pubspec;

import xyz.rk0cc.willpub.pubspec.data.Pubspec;
import xyz.rk0cc.willpub.pubspec.data.PubspecSnapshot;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.*;
import java.util.Stack;

public final class PubspecArchiver {
    private final Stack<PubspecArchiveNode> archives = new Stack<>();
    private final Path projectPath;

    PubspecArchiver(@Nonnull Path projectPath) {
        this.projectPath = projectPath;
    }

    public Path projectPath() {
        return projectPath;
    }

    public void archivePubspec(@Nonnull Pubspec pubspec) {
        archives.push(new PubspecArchiveNode(PubspecSnapshot.getSnapshotOfCurrentPubspec(pubspec)));

        while (archives.size() > 10) // Limited archived object to same memory space.
            archives.remove(0);
    }

    public void undoArchive() {
        if (archives.size() <= 1)
            throw new IndexOutOfBoundsException("Undo required two or more pubspec snapshot are archived.");
        archives.pop();
    }

    public void undoArchive(int count) {
        for (int r = 0; r < count && archives.size() > 1; r++)
            archives.pop();
    }

    public void undoArchive(@Nonnull ZonedDateTime untilBefore) {
        assert untilBefore.getZone().getId().equalsIgnoreCase("UTC");
        while (archives.peek().archivedAtUTC().isAfter(untilBefore) && archives.size() > 1) {
            archives.pop();
        }
    }

    public void clearOlderArchive() {
        PubspecArchiveNode latest = archives.peek();
        archives.clear();
        archives.push(latest);
    }

    @Nonnull
    public LocalDateTime recentArchiveAt() {
        return archives.peek().archivedAt();
    }

    @Nonnull
    public ZonedDateTime recentArchiveAtUTC() {
        return archives.peek().archivedAtUTC();
    }

    @Nonnull
    PubspecSnapshot recentSnapshot() {
        return archives.peek().snapshot();
    }

    private static final class PubspecArchiveNode implements Serializable {
        private final LocalDateTime archivedAt;
        private final PubspecSnapshot snapshot;

        private PubspecArchiveNode(@Nonnull PubspecSnapshot snapshot) {
            this.archivedAt = LocalDateTime.now();
            this.snapshot = snapshot;
        }

        @Nonnull
        public LocalDateTime archivedAt() {
            return archivedAt;
        }

        @Nonnull
        public ZonedDateTime archivedAtUTC() {
            return archivedAt.atZone(ZoneId.of("UTC"));
        }

        public PubspecSnapshot snapshot() {
            return snapshot;
        }
    }
}
