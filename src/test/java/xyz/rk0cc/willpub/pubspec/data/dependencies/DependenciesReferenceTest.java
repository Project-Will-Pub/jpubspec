package xyz.rk0cc.willpub.pubspec.data.dependencies;

import org.junit.jupiter.api.*;
import xyz.rk0cc.jogu.GitRepositoryURL;
import xyz.rk0cc.jogu.UnknownGitRepositoryURLTypeException;
import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;
import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubspecConfigurationException;
import xyz.rk0cc.willpub.pubspec.data.dependencies.type.GitReference;
import xyz.rk0cc.willpub.pubspec.data.dependencies.type.HostedReference;
import xyz.rk0cc.willpub.pubspec.data.dependencies.type.SDKReference;

import javax.annotation.Nonnull;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SuspiciousMethodCalls")
final class DependenciesReferenceTest {
    @Nonnull
    private static DependenciesReferenceSet createEmptySet() {
        return new ImportedReferenceSet();
    }

    @Nonnull
    private static DependenciesReferenceSet createSampleSet()
            throws IllegalPubspecConfigurationException, UnknownGitRepositoryURLTypeException {
        DependenciesReferenceSet drs = createEmptySet();
        drs.add(new HostedReference("path", PubSemVerConstraint.parse("^1.8.0")));
        drs.add(new SDKReference("flutter", "flutter"));
        drs.add(new HostedReference("url_launcher", PubSemVerConstraint.parse("^6.0.18")));
        drs.add(new HostedReference("sembast", PubSemVerConstraint.parse("^3.1.1")));
        drs.add(new GitReference(
                "window_size",
                GitRepositoryURL.parse("git@github.com:google/flutter-desktop-embedding.git"),
                "plugins/window_size",
                "master"
        ));
        return drs;
    }

    @DisplayName("Add dependencies data test")
    @Test
    void testAdd() {
        DependenciesReferenceSet drs = createEmptySet();
        try {
            assertTrue(drs.add(new HostedReference("path", PubSemVerConstraint.parse("^1.8.0"))));
            assertFalse(drs.add(new HostedReference("path", PubSemVerConstraint.parse("^1.8.1"))));
            assertFalse(drs.add(new GitReference("path", GitRepositoryURL.parse("git@example.com:dart_path.git"))));
        } catch (Exception e) {
            fail(e);
        }
    }

    @DisplayName("Set dependencies data test")
    @Test
    void testSet() {
        DependenciesReferenceSet drs = createEmptySet();
        try {
            assertTrue(drs.set(new HostedReference("path", PubSemVerConstraint.parse("^1.8.0"))));
            assertTrue(drs.set(new HostedReference("path", PubSemVerConstraint.parse("^1.8.1"))));
            assertTrue(drs.set(new GitReference("path", GitRepositoryURL.parse("git@example.com:dart_path.git"))));
        } catch (Exception e) {
            fail(e);
        }
    }

    @DisplayName("Retain named dependencies")
    @Test
    void testRetainAll() {
        try {
            DependenciesReferenceSet drs = createSampleSet();
            assertTrue(drs.retainAll(Set.of("flutter", "sembast", "path")));
            assertEquals(3, drs.size());
        } catch (IllegalPubspecConfigurationException | UnknownGitRepositoryURLTypeException e) {
            fail(e);
        }
    }

    @DisplayName("Remove all matched dependencies")
    @Test
    void testRemoveAll() {
        try {
            DependenciesReferenceSet drs = createSampleSet();
            assertTrue(drs.removeAll(Set.of("flutter", "sembast", "path")));
            assertEquals(2, drs.size());
        } catch (IllegalPubspecConfigurationException | UnknownGitRepositoryURLTypeException e) {
            fail(e);
        }
    }
}
