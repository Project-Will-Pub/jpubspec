package xyz.rk0cc.willpub.pubspec.parser;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Resolving {@link ObjectMapper} with no factory implemented which suitable for parsing JSON format only.
 *
 * @since 1.2.0
 */
public final class PubspecJSONParser {
    /**
     * Generate {@link ObjectMapper} with no factory implemented.
     *
     * @return {@link ObjectMapper} for resolving JSON format.
     */
    public static ObjectMapper getParser() {
        return new ObjectMapper().registerModule(PubspecParser.pubsepcModule());
    }
}
