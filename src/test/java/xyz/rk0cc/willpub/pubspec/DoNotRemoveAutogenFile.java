package xyz.rk0cc.willpub.pubspec;

import java.lang.annotation.*;

/**
 * Annotate this if keeping any auto generated {@link java.io.File} which generated automatically.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DoNotRemoveAutogenFile {
}
