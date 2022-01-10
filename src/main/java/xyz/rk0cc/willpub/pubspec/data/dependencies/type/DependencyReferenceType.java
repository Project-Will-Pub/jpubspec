package xyz.rk0cc.willpub.pubspec.data.dependencies.type;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

public abstract class DependencyReferenceType implements Serializable {
    private final String name;

    DependencyReferenceType(@Nonnull String name) {
        this.name = name;
    }

    public final String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DependencyReferenceType that = (DependencyReferenceType) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
