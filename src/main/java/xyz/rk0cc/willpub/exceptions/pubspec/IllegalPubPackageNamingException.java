package xyz.rk0cc.willpub.exceptions.pubspec;

import javax.annotation.Nonnull;

public class IllegalPubPackageNamingException extends IllegalPubspecConfigurationException {
    private static final String DEFAULT_MESSAGE = "This package naming contains one or more invalid charter.";

    public final String illegalPackageName;

    public IllegalPubPackageNamingException(@Nonnull String illegalPackageName, @Nonnull String message) {
        super(message);
        this.illegalPackageName = illegalPackageName;
    }

    public IllegalPubPackageNamingException(@Nonnull String illegalPackageName) {
       this(illegalPackageName, DEFAULT_MESSAGE);
    }
    
    @Override
    public String toString() {
        return super.toString() + "\n\nIllegal package name: " + illegalPackageName;
    }
}
