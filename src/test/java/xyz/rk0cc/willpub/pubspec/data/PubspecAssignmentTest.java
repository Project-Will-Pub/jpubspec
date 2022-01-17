package xyz.rk0cc.willpub.pubspec.data;

import org.junit.jupiter.api.*;
import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;
import xyz.rk0cc.willpub.exceptions.pubspec.*;

import static org.junit.jupiter.api.Assertions.*;

final class PubspecAssignmentTest {
    private static Pubspec mockPubspec;

    @BeforeAll
    static void init() throws IllegalPubspecConfigurationException {
        mockPubspec = new Pubspec(
                "jpubspec_test",
                new PubspecEnvironment(PubSemVerConstraint.parse(">=2.12.0 <3.0.0"))
        );
    }

    @AfterEach
    void nameReset() throws IllegalPubPackageNamingException {
        mockPubspec.modifyName("jpubspec_test");
    }

    @DisplayName("Rename pub package")
    @Test
    void testRename() {
        assertDoesNotThrow(() -> mockPubspec.modifyName("foo_jpubspec"));
        assertThrows(
                IllegalPubPackageNamingException.class,
                () -> mockPubspec.modifyName("X jps")
        );
        assertThrows(
                IllegalPubPackageNamingException.class,
                () -> mockPubspec.modifyName("enum")
        );
    }
}
