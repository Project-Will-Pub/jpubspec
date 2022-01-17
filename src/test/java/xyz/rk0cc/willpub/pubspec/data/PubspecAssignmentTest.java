package xyz.rk0cc.willpub.pubspec.data;

import org.junit.jupiter.api.*;
import xyz.rk0cc.jogu.GitRepositoryURL;
import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;
import xyz.rk0cc.willpub.exceptions.pubspec.*;
import xyz.rk0cc.willpub.pubspec.data.dependencies.type.*;

import java.util.Map;

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
        mockPubspec.dependencies().clear();
        mockPubspec.devDependencies().clear();
        mockPubspec.dependencyOverrides().clear();
        mockPubspec.clearAllAdditionalData();
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

    @DisplayName("Mock adding dependencies")
    @Test
    void testAddDependencies() {
        assertDoesNotThrow(
                () -> mockPubspec.dependencies()
                        .add(new HostedReference("path", PubSemVerConstraint.parse("^1.8.0")))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> mockPubspec.dependencies()
                        .add(new HostedReference("neo_pubspec", PubSemVerConstraint.parse("<2.0.0 >=1.0.0")))
        );
        assertEquals(1, mockPubspec.dependencies().size());
    }

    @DisplayName("Test assigning dependencies overridden")
    @Test
    void testAddOverrideDependencies() {
        assertDoesNotThrow(
                () -> mockPubspec.dependencyOverrides()
                        .add(new HostedReference("path", PubSemVerConstraint.parse("1.8.0")))
        );
        assertDoesNotThrow(
                () -> mockPubspec.dependencyOverrides()
                        .add(new GitReference(
                                "sample_git_pkg",
                                GitRepositoryURL.parse("git://example.com/sgp.git")
                        ))
        );
        assertThrows(
                IllegalVersionConstraintException.class,
                () -> mockPubspec.dependencyOverrides()
                        .add(new HostedReference("sembast", PubSemVerConstraint.parse("^3.1.1+1")))
        );
        assertEquals(2, mockPubspec.dependencyOverrides().size());
    }

    @DisplayName("Test applying additional data")
    @Test
    void testAddAdditionalData() {
        assertDoesNotThrow(() -> mockPubspec.appendAdditionalData("flutter", Map.of(
                "uses-material-design", true
        )));
        assertThrows(
                IllegalArgumentException.class,
                () -> mockPubspec.appendAdditionalData("name", "foo")
        );
    }
}
