package xyz.rk0cc.willpub.pubspec.data;

import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.annotation.Nonnull;

/**
 * Specific this package supported which specific platforms to uses.
 *
 * @since 2.0.0
 *
 * @see <a href="https://dart.dev/tools/pub/pubspec#platforms"><code>platform</code> field in pubspec.</a>
 */
public record PubspecPlatforms(
        boolean android,
        boolean ios,
        boolean linux,
        boolean macos,
        boolean web,
        boolean windows
) {
    /**
     * Detect is marked all platform supported.
     *
     * @return <code>true</code> if all given platform are supported.
     */
    public boolean supportAllPlatforms() {
        return supportAllDesktop() && supportAllMobile() && web;
    }

    /**
     * Detect is supported for all desktop environment for this package.
     *
     * @return <code>true</code> if this package supported all desktop environment.
     */
    public boolean supportAllDesktop() {
        return macos && linux && windows;
    }

    /**
     * Detect is supported for all mobile environment for this package.
     *
     * @return <code>true</code> if this package supported all mobile environment.
     */
    public boolean supportAllMobile() {
        return ios && android;
    }

    @Nonnull
    public PubspecPlatforms modifyAndroid(boolean supported) {
        return new PubspecPlatforms(supported, ios, linux, macos, web, windows);
    }

    @Nonnull
    public PubspecPlatforms modifyIos(boolean supported) {
        return new PubspecPlatforms(android, supported, linux, macos, web, windows);
    }

    @Nonnull
    public PubspecPlatforms modifyLinux(boolean supported) {
        return new PubspecPlatforms(android, ios, supported, macos, web, windows);
    }

    @Nonnull
    public PubspecPlatforms modifyMacos(boolean supported) {
        return new PubspecPlatforms(android, ios, linux, supported, web, windows);
    }

    @Nonnull
    public PubspecPlatforms modifyWeb(boolean supported) {
        return new PubspecPlatforms(android, ios, linux, macos, supported, windows);
    }

    @Nonnull
    public PubspecPlatforms modifyWindows(boolean supported) {
        return new PubspecPlatforms(android, ios, linux, macos, web, supported);
    }

    @Nonnull
    public static PubspecPlatforms createAllSupported() {
        return new PubspecPlatforms(true, true, true, true, true, true);
    }
}
