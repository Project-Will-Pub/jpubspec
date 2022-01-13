package xyz.rk0cc.willpub.pubspec.cmd;

import javax.annotation.Nonnull;
import java.util.*;

public interface PubCmdWithFlags {
    @Nonnull
    Map<String, String> flags();

    @Nonnull
    default String assembleFlags() {
        StringBuilder assembled = new StringBuilder();

        Map<String, String> syncedFM = Collections.synchronizedMap(flags());
        Set<String> flagsSet = syncedFM.keySet();

        synchronized (syncedFM) {
            Iterator<String> itf = flagsSet.iterator();

            while (itf.hasNext()) {
                String fName = itf.next();

                assembled.append("-");
                assembled.append(fName.length() == 1 ? fName : "-" + fName);

                if (itf.hasNext())
                    assembled.append(" ");
            }
        }

        return assembled.toString();
    }
}
