package xyz.rk0cc.willpub.pubspec.data;

import xyz.rk0cc.willpub.exceptions.pubspec.IllegalPubPackageNamingException;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Validating incoming apply data of {@link Pubspec} are following the rules.
 *
 * @since 1.0.0
 */
public final class PubspecValueValidator {
    /**
     * You can not create {@link PubspecValueValidator}.
     */
    private PubspecValueValidator() {}

    /**
     * Check the package naming is not reserve keyword using lowercase with number and underscore only.
     *
     * @param packageName Package name that coming to apply.
     *
     * @return <code>true</code> if is follow.
     *
     * @see <a href="https://dart.dev/tools/pub/pubspec#name">Pubspec naming</a>
     * @see <a href="https://dart.dev/guides/language/language-tour#keywords">Dart's keywords table</a>
     */
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

    /**
     * Check the giving {@link URL#getProtocol()} is <code>HTTP</code> or <code>HTTPS</code>.
     *
     * @param url An {@link URL} for validation.
     *
     * @return <code>true</code> is using <code>HTTP</code> or <code>HTTPS</code>.
     */
    public static boolean httpProtocolOnly(@Nonnull URL url) {
        return Pattern.matches("^https?$", url.getProtocol());
    }

    /**
     * Assertion of {@link PubspecValueValidator}.
     * <br/>
     * All method does not return anything but throw {@link Exception} if returned <code>false</code> from the
     * validator.
     *
     * @since 1.0.0
     */
    public static final class ValueAssertion {
        private ValueAssertion() {}

        /**
         * Asserting package naming.
         *
         * @param packageName Package naming.
         *
         * @throws IllegalPubPackageNamingException When {@link PubspecValueValidator#packageNaming(String)} return
         *                                          <code>false</code>.
         */
        public static void assertPackageNaming(@Nonnull String packageName) throws IllegalPubPackageNamingException {
            if (!packageNaming(packageName)) throw new IllegalPubPackageNamingException(packageName);
        }

        /**
         * Asserting package naming with custom message if throwing {@link IllegalPubPackageNamingException}.
         *
         * @param packageName Package naming.
         * @param assertFailedMessage Message shown if assert failed.
         *
         * @throws IllegalPubPackageNamingException When {@link PubspecValueValidator#packageNaming(String)} return
         *                                          <code>false</code>.
         */
        public static void assertPackageNaming(
                @Nonnull String packageName,
                @Nonnull String assertFailedMessage
        ) throws IllegalPubPackageNamingException {
            if (!packageNaming(packageName))
                throw new IllegalPubPackageNamingException(packageName, assertFailedMessage);
        }
    }
}
