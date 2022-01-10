package xyz.rk0cc.willpub.pubspec.data;

import javax.annotation.Nonnull;
import java.io.Serializable;

public interface PubspecStructre extends Serializable {
    @Nonnull
    String name();

    @Nonnull
    PubspecEnvironment environment();
}
