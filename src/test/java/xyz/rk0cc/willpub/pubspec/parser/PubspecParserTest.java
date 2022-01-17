package xyz.rk0cc.willpub.pubspec.parser;

import org.junit.jupiter.api.*;
import xyz.rk0cc.willpub.pubspec.data.Pubspec;

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
}
