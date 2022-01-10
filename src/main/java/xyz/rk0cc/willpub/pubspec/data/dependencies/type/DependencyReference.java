package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import javax.annotation.Nonnull;
import java.io.Serializable;

public abstract class DependencyReference implements Serializable {
    private final String name;

    DependencyReference(@Nonnull String name) {
        this.name = name;
    }

    public final String name() {
        return name;
    }
}
