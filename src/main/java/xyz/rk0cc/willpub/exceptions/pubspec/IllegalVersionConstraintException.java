package xyz.rk0cc.willpub.exceptions.pubspec;

import xyz.rk0cc.josev.constraint.pub.*;

import javax.annotation.Nonnull;

public class IllegalVersionConstraintException extends IllegalPubspecConfigurationException {
    public final PubSemVerConstraint versionConstraint;

    public IllegalVersionConstraintException(@Nonnull String message, @Nonnull PubSemVerConstraint versionConstraint) {
        super(message);
        this.versionConstraint = versionConstraint;
    }

    @Override
    public String getCausedConfigurationMessage() {
        return "Version constraint: " + versionConstraint.rawConstraint()
                + "\nConstraint pattern: " + versionConstraint.constraintPattern().name();
    }
}
