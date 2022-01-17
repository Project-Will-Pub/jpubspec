# `pubspec.yaml` Java parser and modifier

[![Test](https://github.com/Project-Will-Pub/jpubspec/actions/workflows/test.yml/badge.svg?branch=main)](https://github.com/Project-Will-Pub/jpubspec/actions/workflows/test.yml)
[![CodeQL](https://github.com/Project-Will-Pub/jpubspec/actions/workflows/codeql.yml/badge.svg?branch=main)](https://github.com/Project-Will-Pub/jpubspec/actions/workflows/codeql.yml)

Allowing to read and write `pubspec.yaml` in JVM.

## Usage

```java
import xyz.rk0cc.willpub.pubspec.PubspecManager;
import xyz.rk0cc.willpub.pubspec.data.Pubspec;

import java.nio.file.Paths;

public class SampleApp {
    public static void main(String[] args) throws Exception {
        // Initialize
        PubspecManager manager = new PubspecManager(Paths.get("path", "to", "the", "project", "directory"));

        // Extract pubspec file
        Pubspec pubspec = manager.loadPubspec();
        
        // Modify name
        pubspec.modifyName("jpubspec");
        
        // (Modify other field of pubspec) ...
        
        // Write to pubspec.yaml
        manager.savePubspec(pubspec);
    }
}
```

## License

BSD-3