package xyz.rk0cc.willpub.pubspec.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.*;

/**
 * Construct {@link ObjectMapper} using {@link YAMLFactory}.
 * <br/>
 * Using YAML parser required package <code>jackson-dataformat-yaml</code>.
 *
 * @since 1.2.0
 */
public final class PubspecYAMLParser {
    /**
     * Generate {@link ObjectMapper} which implemented {@link YAMLFactory}.
     *
     * @return {@link ObjectMapper} for resolving YAML format.
     */
    public static ObjectMapper getParser() {
        return new ObjectMapper(new YAMLFactory()
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE)
                .enable(YAMLGenerator.Feature.INDENT_ARRAYS)
                .enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR)
                .enable(YAMLGenerator.Feature.USE_PLATFORM_LINE_BREAKS)
                .disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID)
                .disable(YAMLGenerator.Feature.CANONICAL_OUTPUT)
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        ).registerModule(PubspecParser.pubsepcModule());
    }
}
