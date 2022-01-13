package xyz.rk0cc.willpub.pubspec.cmd;

import java.util.Iterator;
import java.util.List;

public interface PubCmdWithArgs {
    List<String> args();

    default String assembleArgs() {
        Iterator<String> ita = args().iterator();

        return "";
    }
}
