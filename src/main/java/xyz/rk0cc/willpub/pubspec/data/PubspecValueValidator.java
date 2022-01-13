package xyz.rk0cc.willpub.pubspec.data;

import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubPackageNamingException;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class PubspecValueValidator {
    /**
     * You can not create {@link PubspecValueValidator} and always throw {@link UnsupportedOperationException}.
     */
    private PubspecValueValidator() {
        throw new UnsupportedOperationException("Validator contains static method only");
    }

    public static boolean packageNaming(@Nonnull String packageName) {
        return Pattern.matches("^[a-z0-9_]+$", packageName)
                && Stream.of( // Reserve keyword from https://dart.dev/guides/language/language-tour#keywords
                        "assert",
                        "break",
                        "case",
                        "catch",
                        "class",
                        "const",
                        "continue",
                        "default",
                        "do",
                        "else",
                        "enum",
                        "extends",
                        "false",
                        "final",
                        "finally",
                        "for",
                        "if",
                        "in",
                        "is",
                        "new",
                        "null",
                        "rethrow",
                        "return",
                        "super",
                        "switch",
                        "this",
                        "throw",
                        "true",
                        "try",
                        "var",
                        "void",
                        "while",
                        "with"
                )
                .noneMatch(packageName::equals);
    }

    public static boolean httpProtocolOnly(@Nonnull URL url) {
        return Pattern.matches("^https?$", url.getProtocol());
    }

    public static final class ValueAssertion {
        private ValueAssertion() {
            throw new UnsupportedOperationException("No constructor for assertion");
        }

        public static void assertPackageNaming(@Nonnull String packageName) throws IllegalPubPackageNamingException {
            if (!packageNaming(packageName)) throw new IllegalPubPackageNamingException(packageName);
        }

        public static void assertPackageNaming(
                @Nonnull String packageName,
                @Nonnull String assertFailedMessage
        ) throws IllegalPubPackageNamingException {
            if (!packageNaming(packageName))
                throw new IllegalPubPackageNamingException(packageName, assertFailedMessage);
        }
    }
}
