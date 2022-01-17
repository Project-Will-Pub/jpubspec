package xyz.rk0cc.willpub.pubspec.parser;

import org.junit.jupiter.api.*;
import xyz.rk0cc.josev.constraint.pub.PubConstraintPattern;
import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubspecConfigurationException;
import xyz.rk0cc.willpub.pubspec.data.Pubspec;
import xyz.rk0cc.willpub.pubspec.data.dependencies.type.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
final class PubspecParserTest {
    @DisplayName("Test 1.yaml")
    @Order(1)
    @Test
    void testMinimalPubspec() {
        try {
            Pubspec py1 = PubspecParser.PUBSPEC_MAPPER.readValue(getClass().getResource("1.yaml"), Pubspec.class);
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
            Pubspec py2 = PubspecParser.PUBSPEC_MAPPER.readValue(getClass().getResource("2.yaml"), Pubspec.class);
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
}
