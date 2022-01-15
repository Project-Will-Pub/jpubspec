package xyz.rk0cc.willpub.pubspec.data;

import xyz.rk0cc.josev.constraint.pub.*;
import xyz.rk0cc.willpub.exceptions.pubspec.IllegalVersionConstraintException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Giving a requirement preference which indicating required SDK versions constraint is supported.
 * <br/>
 * Although it has "setter" methods implemented, it's only creating new {@link PubspecEnvironment} with given data
 * to be modified only, just like {@link String}.
 *
 * @since 1.0.0
 *
 * @see <a href="https://dart.dev/tools/pub/pubspec#sdk-constraints">SDK constraint in Dart documentation</a>
 */
public final class PubspecEnvironment implements Serializable {
    private final PubSemVerConstraint sdk, flutter;

    /**
     * Create new environment preference of pubspec's environment.
     *
     * @param sdk Dart SDK constraint.
     * @param flutter Flutter constraint, <code>null</code> if not applied.
     *
     * @throws IllegalVersionConstraintException Applying <code>sdk</code> with {@link PubConstraintPattern#CARET} or
     *                                           missing {@link PubSemVerConstraint#start() lower constraint}.
     */
    public PubspecEnvironment(@Nonnull PubSemVerConstraint sdk, @Nullable PubSemVerConstraint flutter)
            throws IllegalVersionConstraintException {
        if (sdk.constraintPattern() == PubConstraintPattern.CARET)
            throw new IllegalVersionConstraintException("SDK version constraint don't accept caret syntax", sdk);
        else if (sdk.start() == null)
            throw new IllegalVersionConstraintException("Lower constraint must be provided in SDK", sdk);

        this.sdk = sdk;
        this.flutter = flutter;
    }

    /**
     * Create new environment preference of pubspec's environment without Flutter constraint.
     *
     * @param sdk Dart SDK constraint.
     *
     * @throws IllegalVersionConstraintException Applying <code>sdk</code> with {@link PubConstraintPattern#CARET} or
     *                                           missing {@link PubSemVerConstraint#start() lower constraint}.
     */
    public PubspecEnvironment(@Nonnull PubSemVerConstraint sdk) throws IllegalVersionConstraintException {
        this(sdk, null);
    }

    /**
     * Get current SDK constraint.
     *
     * @return A SDK version constraint data.
     */
    @Nonnull
    public PubSemVerConstraint sdk() {
        return sdk;
    }

    /**
     * Get current Flutter constraint.
     *
     * @return A Flutter version constraint data, <code>null</code> if not applied.
     */
    @Nullable
    public PubSemVerConstraint flutter() {
        return flutter;
    }

    /**
     * Apply specified SDK constraint.
     *
     * @param sdk Indicate which SDK version will be applied.
     *
     * @return Another {@link PubspecEnvironment} which contains preferred SDK constraint.
     *
     * @throws IllegalVersionConstraintException Applying <code>sdk</code> with {@link PubConstraintPattern#CARET} or
     *                                           missing {@link PubSemVerConstraint#start() lower constraint}.
     */
    @Nonnull
    public PubspecEnvironment changeSDK(@Nonnull PubSemVerConstraint sdk) throws IllegalVersionConstraintException {
        return new PubspecEnvironment(sdk, this.flutter);
    }

    /**
     * Apply specified Flutter constraint.
     *
     * @param flutter Indicate which Flutter version will be applied, <code>null</code> when won't prefer.
     *
     * @return Another {@link PubspecEnvironment} which contains preferred Flutter constraint.
     */
    @Nonnull
    public PubspecEnvironment changeFlutter(@Nullable PubSemVerConstraint flutter) {
        try {
            return new PubspecEnvironment(this.sdk, flutter);
        } catch (IllegalVersionConstraintException e) {
            throw new AssertionError("Flutter does not affected with illegal version constraint exception", e);
        }
    }
}
