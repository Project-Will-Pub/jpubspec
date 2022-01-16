package xyz.rk0cc.willpub.exceptions.pubspec;

import xyz.rk0cc.josev.constraint.pub.*;

import javax.annotation.Nonnull;

/**
 * Applying {@link PubSemVerConstraint} which unaccepted.
 * <br/>
 * It can be unacceptable {@link PubSemVerConstraint#constraintPattern() pattern} parsed or missing partition of
 * constraint depending on implementer's description.
 *
 * @since 1.0.0
 */
public class IllegalVersionConstraintException extends IllegalPubspecConfigurationException {
    /**
     * A {@link PubSemVerConstraint version constraint} cause this exception throw.
     */
    public final PubSemVerConstraint versionConstraint;

    /**
     * Create new exception with a reason why reject this version constraint.
     *
     * @param message Message of why this exception throw with reason.
     * @param versionConstraint Invalid version constraint.
     */
    public IllegalVersionConstraintException(@Nonnull String message, @Nonnull PubSemVerConstraint versionConstraint) {
        super(message);
        this.versionConstraint = versionConstraint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCausedConfigurationMessage() {
        return "Version constraint: " + versionConstraint.rawConstraint()
                + "\nConstraint pattern: " + versionConstraint.constraintPattern().name();
    }
}
