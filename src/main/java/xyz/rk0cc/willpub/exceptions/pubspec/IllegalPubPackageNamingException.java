package xyz.rk0cc.willpub.exceptions.pubspec;

import xyz.rk0cc.willpub.pubspec.data.PubspecValueValidator;

import javax.annotation.Nonnull;

/**
 * Applied {@link xyz.rk0cc.willpub.pubspec.data.Pubspec#name()} or
 * {@link xyz.rk0cc.willpub.pubspec.data.dependencies.type.DependencyReference#name()} without following naming policy
 * of pubspec.
 *
 * @since 1.0.0
 */
public class IllegalPubPackageNamingException extends IllegalPubspecConfigurationException {
    private static final String DEFAULT_MESSAGE = "This package naming contains one or more invalid charter.";

    /**
     * Package name with illegal characters inside.
     */
    public final String illegalPackageName;

    /**
     * Create new {@link IllegalPubPackageNamingException} and giving custom message to display.
     *
     * @param illegalPackageName Package name which is illegal.
     * @param message Display message when this thrown.
     */
    public IllegalPubPackageNamingException(@Nonnull String illegalPackageName, @Nonnull String message) {
        super(message);
        assert !PubspecValueValidator.packageNaming(illegalPackageName);
        this.illegalPackageName = illegalPackageName;
    }

    /**
     * Create new {@link IllegalPubPackageNamingException} with default message applied.
     *
     * @param illegalPackageName Package name which is illegal.
     */
    public IllegalPubPackageNamingException(@Nonnull String illegalPackageName) {
       this(illegalPackageName, DEFAULT_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCausedConfigurationMessage() {
        return "Illegal package name: " + illegalPackageName;
    }
}
