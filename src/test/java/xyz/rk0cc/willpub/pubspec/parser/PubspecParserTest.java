package xyz.rk0cc.willpub.pubspec.parser;

import org.junit.jupiter.api.*;
import xyz.rk0cc.josev.NonStandardSemVerException;
import xyz.rk0cc.josev.constraint.pub.PubConstraintPattern;
import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;
import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubspecConfigurationException;
import xyz.rk0cc.willpub.pubspec.DoNotRemoveAutogenFile;
import xyz.rk0cc.willpub.pubspec.data.Pubspec;
import xyz.rk0cc.willpub.pubspec.data.PubspecEnvironment;
import xyz.rk0cc.willpub.pubspec.data.dependencies.type.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DoNotRemoveAutogenFile
final class PubspecParserTest {
    @DisplayName("Test 1.yaml")
    @Order(1)
    @Test
    void testMinimalPubspec() {
        try {
            Pubspec py1 = PubspecYAMLParser.getParser().readValue(getClass().getResource("1.yaml"), Pubspec.class);
            assertEquals(py1.name(), "mock_pubspec_package_1");
            assertEquals(py1.description(), "Unexisted pubspec package for jpubspec tessting purpose");
            assertEquals(py1.environment().sdk().rawConstraint(), ">=2.12.0 <3.0.0");
            assertEquals(py1.version().value(), "1.0.0+1");
            assertEquals(py1.dependencies().size(), 0);
            assertEquals(py1.publishTo(), "none");
        } catch (IOException e) {
            fail(e);
        }
    }

    @DisplayName("Test 2.yaml")
    @Order(2)
    @Test
    void testBasicPubspec() {
        try {
            Pubspec py2 = PubspecYAMLParser.getParser().readValue(getClass().getResource("2.yaml"), Pubspec.class);
            assertEquals(py2.name(), "mock_cupertino_calendar");
            assertEquals(py2.description(), "A calendar widget special design for Cupertino UI in Flutter which uses as test");
            assertEquals(py2.environment().sdk().rawConstraint(), ">=2.14.0 <3.0.0");
            assertEquals(py2.environment().flutter().rawConstraint(), ">=2.5.0");
            assertEquals(py2.environment().flutter().constraintPattern(), PubConstraintPattern.TRADITIONAL);
            assertEquals(py2.version().value(), "1.0.0+1");
            assertEquals(py2.homepage().toString(), "https://www.rk0cc.xyz");
            assertEquals(py2.repository().toString(), "https://github.com/rk0cc/cupertino_calendar");
            assertEquals(py2.issueTracker().toString(), "https://github.com/rk0cc/cupertino_calendar/issues");
            assertEquals(py2.dependencies().size(), 4);
            assertDoesNotThrow(() -> py2.dependencies().get("flutter", SDKReference.class));
            assertEquals(py2.dependencies()
                    .get("cupertino_calendar_structre", HostedReference.class)
                    .versionConstraint()
                    .rawConstraint(),
                    "^1.1.1"
            );
            assertNull(py2.publishTo());
            assertTrue(py2.containsKeyInAdditionalData("flutter"));
        } catch (IOException | IllegalPubspecConfigurationException e) {
            fail(e);
        }
    }

    @DisplayName("Test create YAML by Java only")
    @Order(3)
    @Test
    void testWrite() {
        try {
            File pendingWriteFile = Paths.get(getClass().getResource("PubspecParserTest.class").toURI())
                            .getParent()
                            .resolve("auto1.yaml")
                            .toFile();

            System.out.println(pendingWriteFile.getAbsolutePath());

            if (!pendingWriteFile.exists()) {
                pendingWriteFile.createNewFile();
            }

            Pubspec wP = new Pubspec("autogen_pubspec", new PubspecEnvironment(
                    PubSemVerConstraint.parse(">=2.15.0 <3.0.0")
            ));

            wP.modifyDescription("I created by jpubspec lol.");
            wP.modifyVersion("1.2.3");
            wP.dependencies().set(new HostedReference("path", PubSemVerConstraint.parse("^1.8.0")));
            wP.modifyAdditionalData("flutter", null);

            PubspecYAMLParser.getParser().writeValue(pendingWriteFile, wP);

            assertTrue(true);
        } catch (
                IllegalPubspecConfigurationException | IOException | NonStandardSemVerException | URISyntaxException e
        ) {
            fail(e);
        }
    }

    @AfterAll
    static void cleanAutogenFile() {
        Set<String> autoGenFileName = Set.of(
                "auto1.yaml"
        );
        if (PubspecParserTest.class.getDeclaredAnnotation(DoNotRemoveAutogenFile.class) == null) {
            for (String fn : autoGenFileName) {
                try {
                    new File(PubspecParserTest.class.getResource(fn).toURI()).delete();
                } catch (URISyntaxException | NullPointerException | SecurityException e) {
                    System.out.println("\u001B[33mDelete operation failed due to exception thrown, ignore " + fn + "\u001B[33m");
                }
            }
        }
    }
}
