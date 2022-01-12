package xyz.rk0cc.willpub.pubspec.data;

import xyz.rk0cc.josev.constraint.pub.*;
import xyz.rk0cc.willpub.exceptions.pubspec.IllegalVersionConstraintException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

public final class PubspecEnvironment implements Serializable {
    private final PubSemVerConstraint sdk, flutter;

    public PubspecEnvironment(@Nonnull PubSemVerConstraint sdk, @Nullable PubSemVerConstraint flutter)
            throws IllegalVersionConstraintException {
        if (sdk.constraintPattern() == PubConstraintPattern.CARET)
            throw new IllegalVersionConstraintException("SDK version constraint don't accept caret syntax", sdk);
        else if (sdk.start() == null)
            throw new IllegalVersionConstraintException("Lower constraint must be provided in SDK", sdk);

        this.sdk = sdk;
        this.flutter = flutter;
    }

    public PubspecEnvironment(@Nonnull PubSemVerConstraint sdk) throws IllegalVersionConstraintException {
        this(sdk, null);
    }

    @Nonnull
    public PubSemVerConstraint sdk() {
        return sdk;
    }

    @Nullable
    public PubSemVerConstraint flutter() {
        return flutter;
    }

    @Nonnull
    public PubspecEnvironment changeSDK(@Nonnull PubSemVerConstraint sdk) throws IllegalVersionConstraintException {
        return new PubspecEnvironment(sdk, this.flutter);
    }

    @Nonnull
    public PubspecEnvironment changeFlutter(@Nullable PubSemVerConstraint flutter) {
        try {
            return new PubspecEnvironment(this.sdk, flutter);
        } catch (IllegalVersionConstraintException e) {
            throw new AssertionError("Flutter does not affected with illegal version constraint exception", e);
        }
    }
}
