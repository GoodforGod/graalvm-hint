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
- Generate Options Hints ([native-image.properties](#nativeimagehint))
- Generate Initialization Hints ([native-image.properties](#initializationhint))
- Generate Dynamic Proxy Hints ([dynamic-proxy-hint.json](#dynamicproxyhint))
- Generate JNI Hints ([jni-config.json](#jnihint))
- Generate Link Build Hints ([native-image.properties](#jnihint))

## Dependency :rocket:

Java 11+ is supported.

[**Gradle**](https://mvnrepository.com/artifact/io.goodforgod/graalvm-hint-processor)
```groovy
annotationProcessor "io.goodforgod:graalvm-hint-processor:0.19.0"
compilyOnly "io.goodforgod:graalvm-hint-annotations:0.19.0"
```

[**Maven**](https://mvnrepository.com/artifact/io.goodforgod/graalvm-hint-processor)
```xml
<dependency>
    <dependency>
        <groupId>io.goodforgod</groupId>
        <artifactId>graalvm-hint-annotations</artifactId>
        <version>0.19.0</version>
        <scope>compile</scope>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>io.goodforgod</groupId>
        <artifactId>graalvm-hint-processor</artifactId>
        <version>0.19.0</version>
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
                        <version>0.19.0</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## ReflectionHint

You can read more about GraalVM reflection configuration [in official documentation here](https://www.graalvm.org/reference-manual/native-image/Reflection/).

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

Generated *reflection-config.json*:
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

Generated *reflection-config.json*:
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

## ResourceHint

You can read more about GraalVM resource configuration [in official documentation here](https://www.graalvm.org/reference-manual/native-image/Resources/).

Hint allows generating config for resource files to be included/excluded when building native application.
You can also include bundles into native image using this Hint.

Include Hint:
```java
@ResourceHint(include = { "simplelogger.properties", "application.yml", "*.xml" })
public class ResourceNames {

}
```

Generated *resource-config.json*:
```json
{
  "resources": {
    "includes": [
      { "pattern" : "*.xml" },
      { "pattern": "application.yml" },
      { "pattern": "simplelogger.properties" }
    ]
  }
}
```

Exclude Hint:
```java
@ResourceHint(exclude = { "*.xml" })
public class ResourceNames {

}
```

Generated *resource-config.json*:
```json
{
  "resources": {
    "excludes": [
      { "pattern": "*.xml" }
    ]
  }
}
```

Bundle Hint:
```java
@ResourceHint(bundles = { "your.pkg.Bundle" })
public class ResourceNames {

}
```

Generated *resource-config.json*:
```json
{
  "bundles": [
    { "name": "your.pkg.Bundle" }
  ]
}
```

## NativeImageHint

You can read more about GraalVM native-image options [in official documentation here](https://www.graalvm.org/reference-manual/native-image/Options/).

Hint allows generating config for native-image options and initial application entrypoint.

Simple hint configuration:
```java
@NativeImageHint(entrypoint = EntrypointOnly.class)
public class EntrypointOnly {

    public static void main(String[] args) {}
}
```

Generated *native-image.properties*:
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

Generated *native-image.properties*:
```properties
Args = -H:Class=io.goodforgod.graalvm.hint.processor.Entrypoint -H:Name=myapp \
       -H:+PrintClassInitialization \
       -H:+InlineBeforeAnalysis
```

## InitializationHint

You can read more about GraalVM initialization configuration [in official documentation here](https://www.graalvm.org/reference-manual/native-image/ClassInitialization/).

Hint allows generating config for what classes to instantiate in runtime and what classes to instantiate in compile time.

Initialization hint configuration:
```java
@InitializationHint(value = InitializationHint.InitPhase.BUILD, types = HintOptions.class)
@InitializationHint(value = InitializationHint.InitPhase.RUNTIME, typeNames = "io.goodforgod.graalvm.hint.processor")
public class EntrypointOnly {

}
```

Generated *native-image.properties*:
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

Generated *native-image.properties*:
```properties
Args = -H:Class=io.goodforgod.graalvm.hint.processor.Entrypoint \
       --initialize-at-build-time=io.goodforgod.graalvm.hint.processor.HintOrigin.class \
       --initialize-at-run-time=io.goodforgod.graalvm.hint.processor
```

## DynamicProxyHint

You can read more about GraalVM DynamicProxyHint configuration [in official documentation here](https://www.graalvm.org/reference-manual/native-image/DynamicProxy/).

Use can pass dynamic proxy resources (*-H:DynamicProxyConfigurationResources*) or files (*-H:DynamicProxyConfigurationFiles*) using corresponding options:
```java
@DynamicProxyHint(resources = {"proxy-resource.json"}, files = {"proxy-file.json"})
public class Resource {

}
```

Generated *native-image.properties*:
```properties
Args = -H:DynamicProxyConfigurationFiles=proxy-file.json \
       -H:DynamicProxyConfigurationResources=proxy-resource.json
```

You can configure files yourself using annotations only without the need for manually creating JSON configurations.

```java
@DynamicProxyHint(value = {
        @DynamicProxyHint.Configuration(interfaces = {OptionParser.class, HintOrigin.class}),
        @DynamicProxyHint.Configuration(interfaces = {HintOrigin.class})
})
public class Config {

}
```

Generated *dynamic-proxy-hint-config.json*:
```json
[
  { "interfaces": [ "io.goodforgod.graalvm.hint.processor.OptionParser", "io.goodforgod.graalvm.hint.processor.HintOrigin" ] },
  { "interfaces": [ "io.goodforgod.graalvm.hint.processor.HintOrigin" ] }
]
```

Generated *native-image.properties*:
```properties
Args = -H:DynamicProxyConfigurationResources=META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/dynamic-proxy-config.json
```

## JniHint

You can read more about GraalVM JNI configuration [in official documentation here](https://www.graalvm.org/reference-manual/native-image/JNI/).

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

Generated *jni-config.json*:
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

Generated *jni-config.json*:
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

## LinkHint

## License

This project licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details