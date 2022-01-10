package xyz.rk0cc.willpub.pubspec.data;

import xyz.rk0cc.josev.constraint.pub.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record PubspecEnvironment(@Nonnull PubSemVerConstraint sdk, @Nullable PubSemVerConstraint flutter) {

    public PubspecEnvironment(@Nonnull PubSemVerConstraint sdk, @Nullable PubSemVerConstraint flutter) {
        if (sdk.constraintPattern() == PubConstraintPattern.CARET)
            throw new IllegalArgumentException("SDK can not applied caret syntax version constraint");

        this.sdk = sdk;
        this.flutter = flutter;
    }

    @Nonnull
    public PubspecEnvironment changeSDK(@Nonnull PubSemVerConstraint sdk) {
        return new PubspecEnvironment(sdk, this.flutter);
    }

    @Nonnull
    public PubspecEnvironment changeFlutter(@Nullable PubSemVerConstraint flutter) {
        return new PubspecEnvironment(this.sdk, flutter);
    }
}
