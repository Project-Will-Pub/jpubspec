package xyz.rk0cc.willpub.exceptions.pubspec;

import javax.annotation.Nonnull;

public class IllegalPubspecConfigurationException extends Exception {
    protected IllegalPubspecConfigurationException(@Nonnull String message) {
        super(message);
    }

    protected IllegalPubspecConfigurationException(@Nonnull String message, @Nonnull Throwable throwable) {
        super(message, throwable);
    }
}
