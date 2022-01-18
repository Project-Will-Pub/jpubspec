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

## Layout changes after using JPubspec

Since YAML parser is using Jackson package which based on SnakeYAML, a package which is Java implementation
on YAML **1.1** to make more but not fully compatible on YAML **1.2**, a version that using JSON syntax as standard.

Thus, any string with quoted value will be removed if origin value does not represent another type or toggle capture
symbol. If is, it wrapped by single quote (`'`) when export `Pubspec` to file.

Here is some examples that will be affected if applied:

#### Applying `null` in `HostedReference`'s version constraint

**Origin**

```yaml
name: foo
description: bar
version: 1.0.0+1
environment:
  sdk: '>=2.12.0 <3.0.0'
dependencies:
  path: 
```

**Exported**

```yaml
name: foo
description: bar
version: 1.0.0+1
environment:
  sdk: '>=2.12.0 <3.0.0'
dependencies:
  path: null
```

#### Applying double quote

**Origin**

```yaml
name: foo
description: bar
version: 1.0.0+1
publish_to: "none"
environment:
  sdk: ">=2.12.0 <3.0.0"
```

**Exported**

```yaml
name: foo
description: bar
version: 1.0.0+1
publish_to: none
environment:
  sdk: '>=2.12.0 <3.0.0'
```

## Setup

* Maven 3.8.3 or above
* JDK 17 or above

## License

BSD-3