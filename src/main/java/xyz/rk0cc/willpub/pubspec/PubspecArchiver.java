package xyz.rk0cc.willpub.pubspec;

import xyz.rk0cc.willpub.pubspec.data.Pubspec;
import xyz.rk0cc.willpub.pubspec.data.PubspecSnapshot;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.*;
import java.util.Stack;

/**
 * Archive and store edited {@link Pubspec} as {@link PubspecSnapshot} up to 10 edited version.
 *
 * @since 1.0.0
 */
public final class PubspecArchiver {
    private final Stack<PubspecArchiveNode> archives = new Stack<>();
    private final Path projectPath;

    /**
     * Create new archiver under a project path.
     *
     * @param projectPath Current project path from {@link PubspecManager}.
     */
    PubspecArchiver(@Nonnull Path projectPath) {
        this.projectPath = projectPath;
    }

    /**
     * Current project {@link Path} which handling the archive of {@link Pubspec}.
     *
     * @return {@link Path} of archiving {@link Pubspec}.
     */
    public Path projectPath() {
        return projectPath;
    }

    /**
     * Archive current edited {@link Pubspec} data into the archive.
     * <br/>
     * If current archive has 10 edit history already, the earliest archive one will be removed when this method
     * called.
     *
     * @param pubspec A {@link Pubspec} which current state of data will be archived.
     */
    public void archivePubspec(@Nonnull Pubspec pubspec) {
        archives.push(new PubspecArchiveNode(PubspecSnapshot.getSnapshotOfCurrentPubspec(pubspec)));

        while (archives.size() >= 10) // Limited archived object to same memory space.
            archives.remove(0);
    }

    /**
     * Undo the previous {@link #archivePubspec(Pubspec) archive}.
     *
     * @throws IndexOutOfBoundsException If less or equal than one archive saved in {@link PubspecArchiver}.
     */
    public void undoArchive() {
        if (archives.size() <= 1)
            throw new IndexOutOfBoundsException("Undo required two or more pubspec snapshot are archived.");
        archives.pop();
    }

    /**
     * Undo multiple archives in a count or remain first archive one.
     *
     * @param count Number of older version removed.
     */
    public void undoArchive(int count) {
        for (int r = 0; r < count && archives.size() > 1; r++)
            archives.pop();
    }

    /**
     * Remove archive with given {@link ZonedDateTime} in <b>UTC</b>.
     * 
     * @param untilBefore A UTC of {@link ZonedDateTime} that any archived before that time will not be removed.
     */
    public void undoArchive(@Nonnull ZonedDateTime untilBefore) {
        assert untilBefore.getZone().getId().equalsIgnoreCase("UTC");
        while (archives.peek().archivedAtUTC().isAfter(untilBefore) && archives.size() > 1) {
            archives.pop();
        }
    }

    /**
     * Wipe all {@link #archivePubspec(Pubspec) stored archive} except the latest one.
     */
    public void clearOlderArchive() {
        PubspecArchiveNode latest = archives.peek();
        archives.clear();
        archives.push(latest);
    }

    /**
     * Get recent {@link #archivePubspec(Pubspec) stored archive} date time in local.
     *
     * @return {@link LocalDateTime} of recent archive action at.
     */
    @Nonnull
    public LocalDateTime recentArchiveAt() {
        return archives.peek().archivedAt();
    }

    /**
     * Get recent {@link #archivePubspec(Pubspec) stored archive} date time in UTC.
     *
     * @return {@link ZonedDateTime} of recent archive action at which {@link ZonedDateTime#getZone() zone id} is UTC.
     */
    @Nonnull
    public ZonedDateTime recentArchiveAtUTC() {
        return archives.peek().archivedAtUTC();
    }

    /**
     * Get the latest version of {@link PubspecSnapshot}.
     *
     * @return Latest {@link #archivePubspec(Pubspec) archived} {@link PubspecSnapshot}.
     */
    @Nonnull
    PubspecSnapshot recentSnapshot() {
        return archives.peek().snapshot();
    }
}

/**
 * A node contains {@link PubspecSnapshot} when called {@link PubspecArchiver#archivePubspec(Pubspec)} and
 * {@link Stack#push(Object) push this node}.
 * 
 * @since 1.0.0
 */
final class PubspecArchiveNode implements Serializable {
    private final ZonedDateTime archivedAt;
    private final PubspecSnapshot snapshot;

    /**
     * Create new node of {@link PubspecArchiveNode}.
     *
     * @param snapshot A {@link PubspecSnapshot} data that going to store.
     */
    PubspecArchiveNode(@Nonnull PubspecSnapshot snapshot) {
        this.archivedAt = ZonedDateTime.now(ZoneId.of("UTC"));
        this.snapshot = snapshot;
    }

    /**
     * Node created at in local time.
     *
     * @return {@link LocalDateTime} of create this node.
     */
    @Nonnull
    LocalDateTime archivedAt() {
        return archivedAt.toLocalDateTime();
    }

    /**
     * Node created at in UTC.
     *
     * @return {@link ZonedDateTime} of create this node.
     */
    @Nonnull
    ZonedDateTime archivedAtUTC() {
        return archivedAt;
    }

    /**
     * Snapshot of this node.
     *
     * @return A {@link PubspecSnapshot} when this node is created.
     */
    @Nonnull
    PubspecSnapshot snapshot() {
        return snapshot;
    }
}
