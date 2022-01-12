package xyz.rk0cc.willpub.exceptions.pubspec;

import javax.annotation.Nonnull;

public class IllegalPubspecConfigurationException extends Exception {
    protected IllegalPubspecConfigurationException(@Nonnull String message) {
        super(message);
    }

    protected IllegalPubspecConfigurationException(@Nonnull String message, @Nonnull Throwable throwable) {
        super(message, throwable);
    }

    public String getCausedConfigurationMessage() {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        if (getCausedConfigurationMessage() != null) {
            sb.append("\n\n");
            sb.append(getCausedConfigurationMessage());
        }
        return sb.toString();
    }
}
