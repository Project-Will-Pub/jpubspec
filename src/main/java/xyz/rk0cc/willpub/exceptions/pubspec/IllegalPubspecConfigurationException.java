package xyz.rk0cc.willpub.exceptions.pubspec;

import javax.annotation.Nonnull;

/**
 * An {@link Exception exception} which related with invalid modification on
 * {@link xyz.rk0cc.willpub.pubspec.data.Pubspec}.
 *
 * @since 1.0.0
 */
public class IllegalPubspecConfigurationException extends Exception implements PubspecException {
    /**
     * Create {@link IllegalPubspecConfigurationException} with reason given.
     *
     * @param message Message that to give a reason why this exception thrown.
     */
    protected IllegalPubspecConfigurationException(@Nonnull String message) {
        super(message);
    }

    /**
     * Create {@link IllegalPubspecConfigurationException} with reason given and a {@link Throwable} to throw this
     * exception.
     *
     * @param message Message that to give a reason why this exception thrown.
     * @param throwable Another {@link Throwable} causing this exception thrown.
     */
    protected IllegalPubspecConfigurationException(@Nonnull String message, @Nonnull Throwable throwable) {
        super(message, throwable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCausedConfigurationMessage() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
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
