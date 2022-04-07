# GraalVM Hint Processor

![GraalVM Enabled](https://img.shields.io/badge/GraalVM-Ready-orange?style=plastic)
[![GitHub Action](https://github.com/goodforgod/graalvm-hint/workflows/Java%20CI/badge.svg)](https://github.com/GoodforGod/graalvm-hint/actions?query=workflow%3A%22Java+CI%22)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_graalvm-hint&metric=coverage)](https://sonarcloud.io/dashboard?id=GoodforGod_graalvm-hint)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_graalvm-hint&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=GoodforGod_graalvm-hint)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_graalvm-hint&metric=ncloc)](https://sonarcloud.io/dashboard?id=GoodforGod_graalvm-hint)

GraalVM Hint Processor helps generate GraalVM hints for building [native-image](https://www.graalvm.org/reference-manual/native-image/) applications.

Fully AOT processing library, no dependencies, no runtime side-affects.

Features:
- Generate Reflection Hints ([reflect-config.json](#reflectionhint))
- Generate Resource Hints ([resource-config.json](#resourcehint))
- Generate Options hints ([native-image.properties](#nativeimagehint))
- Generate Initialization Hints ([native-image.properties](#initializationhint))
- Generate JNI Hints ([jni-config.json](#jnihint))

## Dependency :rocket:

Java 11+ is supported.

[**Gradle**](https://mvnrepository.com/artifact/io.goodforgod/graalvm-hint-processor)
```groovy
annotationProcessor "io.goodforgod:graalvm-hint-processor:0.17.1"
compilyOnly "io.goodforgod:graalvm-hint-annotations:0.17.1"
```

[**Maven**](https://mvnrepository.com/artifact/io.goodforgod/graalvm-hint-processor)
```xml
<dependency>
    <dependency>
        <groupId>io.goodforgod</groupId>
        <artifactId>graalvm-hint-annotations</artifactId>
        <version>0.17.1</version>
        <scope>compile</scope>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>io.goodforgod</groupId>
        <artifactId>graalvm-hint-processor</artifactId>
        <version>0.17.1</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.10.0</version>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>io.goodforgod</groupId>
                        <artifactId>graalvm-hint-processor</artifactId>
                        <version>0.17.1</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Examples

Below are illustrated examples for usage of such library.

### ReflectionHint

You can read more about GraalVM reflection configuration [here](https://www.graalvm.org/reference-manual/native-image/Reflection/).

There are available access hints:
- allPublicFields
- allPublicMethods
- allPublicConstructors
- allDeclaredFields
- allDeclaredMethods
- allDeclaredConstructors

Generating reflection access, most used cases is DTOs that are used for serialization/deserialization in any format (JSON for example).

Simple case for single Java class:
```java
@ReflectionHint
public class RequestOnly {

    private String name;
}
```

Generated reflection config:
```json
[{
  "name": "io.goodforgod.graalvm.hint.processor.RequestOnly",
  "allDeclaredConstructors": true,
  "allDeclaredFields": true,
  "allDeclaredMethods": true
}]
```

There may be more different cases, like generating hints for classes that are package private or just private, also there hint can be used for whole package.

Complex example generating reflection access:
```java
@ReflectionHint(types = { Response.class, Request.class }, value = ReflectionHint.AccessType.ALL_DECLARED_FIELDS)
@ReflectionHint(typeNames = { "io.goodforgod.graalvm.hint.processor"})
public class ReflectionConfig {

    private String name;
}
```

Resulted reflection-config.json:
```json
[{
  "name": "io.goodforgod.graalvm.hint.processor",
  "allDeclaredConstructors": true,
  "allDeclaredFields": true,
  "allDeclaredMethods": true
},
{
  "name": "io.goodforgod.example.Response",
  "allDeclaredFields": true
},
{
  "name": "io.goodforgod.example.Request",
  "allDeclaredFields": true
}]
```

### ResourceHint

You can read more about GraalVM resource configuration [here](https://www.graalvm.org/reference-manual/native-image/Resources/).

Hint allows generating config for resource files to include into native application.

Hint configuration:
```java
@ResourceHint(patterns = { "simplelogger.properties", "application.yml", "*.xml" })
public class ResourceNames {

}
```

Resulted resource-config.json:
```json
{
  "resources": [
    { "pattern" : "*.xml" },
    { "pattern" : "application.yml" },
    { "pattern" : "simplelogger.properties" }
  ]
}
```

### NativeImageHint

You can read more about GraalVM native-image options [here](https://www.graalvm.org/reference-manual/native-image/Options/).

Hint allows generating config for native-image options and initial application entrypoint.

Simple hint configuration:
```java
@NativeImageHint(entrypoint = EntrypointOnly.class)
public class EntrypointOnly {

    public static void main(String[] args) {}
}
```

Resulted native-image.properties:
```properties
Args = -H:Class=io.goodforgod.graalvm.hint.processor.EntrypointOnly
```

Complex hint configuration with options:
```java
@NativeImageHint(entrypoint = Entrypoint.class, name = "myapp", options = { NativeImageOptions.PRINT_INITIALIZATION, NativeImageOptions.INLINE_BEFORE_ANALYSIS })
public class Entrypoint {

    public static void main(String[] args) {}
}
```

Resulted native-image.properties:
```properties
Args = -H:Name=myapp -H:Class=io.goodforgod.graalvm.hint.processor.Entrypoint \
       -H:+PrintClassInitialization \
       -H:+InlineBeforeAnalysis
```

### InitializationHint

You can read more about GraalVM initialization configuration [here](https://www.graalvm.org/reference-manual/native-image/ClassInitialization/).

Hint allows generating config for what classes to instantiate in runtime and what classes to instantiate in compile time.

Initialization hint configuration:
```java
@InitializationHint(value = InitializationHint.InitPhase.BUILD, types = HintOptions.class)
@InitializationHint(value = InitializationHint.InitPhase.RUNTIME, typeNames = "io.goodforgod.graalvm.hint.processor")
public class EntrypointOnly {

}
```

Resulted native-image.properties:
```properties
Args = --initialize-at-build-time=io.goodforgod.graalvm.hint.processor.HintOrigin.class \
       --initialize-at-run-time=io.goodforgod.graalvm.hint.processor
```

Options and initialization hint configuration:
```java
@NativeImageHint(entrypoint = Entrypoint.class)
@InitializationHint(value = InitializationHint.InitPhase.BUILD, types = HintOptions.class)
@InitializationHint(value = InitializationHint.InitPhase.RUNTIME, typeNames = "io.goodforgod.graalvm.hint.processor")
public class Entrypoint {

    public static void main(String[] args) {}
}
```

Resulted native-image.properties:
```properties
Args = -H:Class=io.goodforgod.graalvm.hint.processor.Entrypoint \
       --initialize-at-build-time=io.goodforgod.graalvm.hint.processor.HintOrigin.class \
       --initialize-at-run-time=io.goodforgod.graalvm.hint.processor
```

### JniHint

You can read more about GraalVM JNI configuration [here](https://www.graalvm.org/reference-manual/native-image/JNI/).

There are available JNI access hints:
- allPublicFields
- allPublicMethods
- allPublicConstructors
- allDeclaredFields
- allDeclaredMethods
- allDeclaredConstructors

Simple case for single Java class:
```java
@JniHint
public class RequestOnly {

    private String name;
}
```

Generated JNI config:
```json
[{
  "name": "io.goodforgod.graalvm.hint.processor.RequestOnly",
  "allDeclaredConstructors": true,
  "allDeclaredFields": true,
  "allDeclaredMethods": true
}]
```

There may be more different cases, like generating hints for classes that are package private or just private, also there hint can be used for whole package.

Complex example generating reflection access:
```java
@JniHint(types = { Response.class, Request.class }, value = JniHint.AccessType.ALL_DECLARED_FIELDS)
@JniHint(typeNames = { "io.goodforgod.graalvm.hint.processor"})
public final class JniConfig {

}
```

Resulted jni-config.json:
```json
[{
  "name": "io.goodforgod.graalvm.hint.processor",
  "allDeclaredConstructors": true,
  "allDeclaredFields": true,
  "allDeclaredMethods": true
},
{
  "name": "io.goodforgod.example.Response",
  "allDeclaredFields": true
},
{
  "name": "io.goodforgod.example.Request",
  "allDeclaredFields": true
}]
```

## License

This project licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details