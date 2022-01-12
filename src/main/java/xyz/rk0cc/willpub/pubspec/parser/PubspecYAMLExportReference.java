package xyz.rk0cc.willpub.pubspec.parser;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PubspecYAMLExportReference {
    /**
     * Declare which {@link PubspecFormat} do not affected when exporting {@link xyz.rk0cc.willpub.pubspec.data.Pubspec}
     * to YAML file.
     *
     * @return A {@link PubspecFormat} array which do not apply during export.
     */
    PubspecFormat[] disableFormat() default {};
}
