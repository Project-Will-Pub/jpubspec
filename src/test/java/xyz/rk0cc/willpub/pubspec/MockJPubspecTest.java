package xyz.rk0cc.willpub.pubspec;

import org.junit.jupiter.api.*;
import xyz.rk0cc.josev.NonStandardSemVerException;
import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;
import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubspecConfigurationException;
import xyz.rk0cc.willpub.pubspec.data.*;
import xyz.rk0cc.willpub.pubspec.data.dependencies.type.HostedReference;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ConstantConditions")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
final class MockJPubspecTest {
    private static PubspecManager mgr;
    private static Pubspec pubspec;
    private static PubspecSnapshot originContext;

    @BeforeAll
    static void init() throws Exception {
        Path pdPath = Paths.get("src", "test", "resources", "mock_pub").toAbsolutePath();
        mgr = new PubspecManager(pdPath);
        pubspec = mgr.loadPubspec();
        originContext = PubspecSnapshot.getSnapshotOfCurrentPubspec(pubspec);
    }

    @DisplayName("Edit version and save")
    @Order(1)
    @Test
    void testEditVersion() {
        try {
            pubspec.modifyVersion("1.0.1");
            mgr.savePubspec(pubspec);

            assertEquals(
                    Files.readString(Paths.get(getClass().getResource("expected1.yaml").toURI())),
                    Files.readString(Paths.get(mgr.pubspecYAML().getAbsolutePath()))
            );
        } catch (NonStandardSemVerException | IOException | URISyntaxException e) {
            fail(e);
        }
    }

    @DisplayName("Edit publish to and dependencies")
    @Order(2)
    @Test
    void testEditPublishAndDep() {
        try {
            pubspec.modifyPublishTo("none");
            pubspec.dependencies().set(new HostedReference("path", PubSemVerConstraint.parse("^1.8.0")));
            mgr.savePubspec(pubspec);

            assertEquals(
                    Files.readString(Paths.get(getClass().getResource("expected2.yaml").toURI())),
                    Files.readString(Paths.get(mgr.pubspecYAML().getAbsolutePath()))
            );
        } catch (IllegalPubspecConfigurationException | IOException | URISyntaxException e) {
            fail(e);
        }
    }

    @DisplayName("Recover pubspec data")
    @Order(3)
    @Test
    void recoverTest() {
        mgr.archiver().undoArchive();
        try {
            mgr.savePubspecFromLatestArchive();

            assertEquals(
                    Files.readString(Paths.get(getClass().getResource("expected1.yaml").toURI())),
                    Files.readString(Paths.get(mgr.pubspecYAML().getAbsolutePath()))
            );
        } catch (IOException | URISyntaxException e) {
            fail(e);
        }
    }

    @AfterEach
    void archiveEachModify() {
        mgr.archiver().archivePubspec(pubspec);
    }

    @AfterAll
    static void post() throws IOException {
        mgr.savePubspec(PubspecSnapshot.getMutableFromSnapshot(originContext));
    }
}
