package xyz.rk0cc.willpub.pubspec.data;

import xyz.rk0cc.josev.SemVer;
import xyz.rk0cc.willpub.pubspec.data.dependencies.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.util.Map;

interface PubspecStructure {
    @Nonnull
    String name();

    @Nonnull
    PubspecEnvironment environment();

    @Nullable
    SemVer version();

    @Nullable
    String description();

    @Nullable
    String publishTo();

    @Nullable
    URL homepage();

    @Nullable
    URL repository();

    @Nullable
    URL issueTracker();

    @Nullable
    URL documentation();

    @Nonnull
    ImportedReferenceSet dependencies();

    @Nonnull
    ImportedReferenceSet devDependencies();

    @Nonnull
    OverrideReferenceSet dependencyOverrides();

    @Nonnull
    Map<String, Object> additionalData();
}
