package xyz.rk0cc.willpub.pubspec.data;

import org.junit.jupiter.api.*;
import xyz.rk0cc.josev.SemVer;
import xyz.rk0cc.josev.constraint.pub.PubSemVerConstraint;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class JsonLikeDataTypeTest {
    @DisplayName("JSON value")
    @Test
    void testNonContainerType() {
        assertTrue(PermitAdditionalMapValue.isJsonLiked("Foo"));
        assertTrue(PermitAdditionalMapValue.isJsonLiked('a'));
        assertTrue(PermitAdditionalMapValue.isJsonLiked(false));
        assertTrue(PermitAdditionalMapValue.isJsonLiked(null));
        assertTrue(PermitAdditionalMapValue.isJsonLiked(1));
        assertTrue(PermitAdditionalMapValue.isJsonLiked(2.4f));
        assertTrue(PermitAdditionalMapValue.isJsonLiked(3.6d));
        assertTrue(PermitAdditionalMapValue.isJsonLiked(9.8D));
        assertFalse(PermitAdditionalMapValue.isJsonLiked(ZonedDateTime.now()));
    }

    @DisplayName("Wrap in JSON array")
    @Test
    void testContainInList() {
        assertTrue(PermitAdditionalMapValue.isJsonLiked(List.of(1, 3d, 4.5f, 6.7d, "LOL", 'g')));
        assertFalse(PermitAdditionalMapValue.isJsonLiked(List.of("Hi", 'd', 5.4f, new SemVer(1))));
    }

    @DisplayName("Wrap in JSON object")
    @Test
    void testContainInMap() {
        final LinkedHashMap<String, Object> validValueJson = new LinkedHashMap<>();
        validValueJson.put("foo", "bar");
        validValueJson.put("lol", 'd');
        validValueJson.put("integer", 9);
        validValueJson.put("float", 4.0f);
        validValueJson.put("64", 98L);
        validValueJson.put("nothing", null);

        final LinkedHashMap<String, Object> invalidValueJson = new LinkedHashMap<>();
        invalidValueJson.put("ohayo", "gm");
        invalidValueJson.put("vc", PubSemVerConstraint.parse("^1.0.0"));

        final LinkedHashMap<Object, Object> incompatableJson = new LinkedHashMap<>();
        incompatableJson.put(1, 4);
        incompatableJson.put('E', null);

        assertTrue(PermitAdditionalMapValue.isJsonLiked(validValueJson));
        assertFalse(PermitAdditionalMapValue.isJsonLiked(invalidValueJson));
        assertFalse(PermitAdditionalMapValue.isJsonLiked(incompatableJson));
    }

    @DisplayName("Replicating JSON object with Java type")
    @Test
    void testMixedContainerAndValue() {
        final LinkedHashMap<String, Object> mockJson = new LinkedHashMap<>();
        mockJson.put("name", "Angle Home Handsome");
        mockJson.put("age", 30);
        mockJson.put("length", 193.0f);
        mockJson.put("career", List.of(
                Map.of(
                        "job", "sales",
                        "from", 2013,
                        "to", 2018
                ),
                Map.of(
                        "job", "artist",
                        "from", 2018,
                        "agent", "WeuTV",
                        "group", "FAULT",
                        "otherMembers", List.of(
                                "'Chubby' man",
                                "PK boi",
                                "Day day 9 wu"
                        )
                )
        ));
        mockJson.put("attitude", "D-");
        mockJson.put("issue", List.of(
                "Rude",
                "Cult of personality",
                "Swearing to customer"
        ));

        assertTrue(PermitAdditionalMapValue.isJsonLiked(mockJson));
    }
}
