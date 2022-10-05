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
- Generate Link Build Hints ([native-image.properties](#linkhint))

## Dependency :rocket:

Java 11+ is supported.

[**Gradle**](https://mvnrepository.com/artifact/io.goodforgod/graalvm-hint-processor)
```groovy
annotationProcessor "io.goodforgod:graalvm-hint-processor:0.20.0"
compilyOnly "io.goodforgod:graalvm-hint-annotations:0.20.0"
```

[**Maven**](https://mvnrepository.com/artifact/io.goodforgod/graalvm-hint-processor)
```xml
<dependencies>
    <dependency>
        <groupId>io.goodforgod</groupId>
        <artifactId>graalvm-hint-annotations</artifactId>
        <version>0.20.0</version>
        <scope>compile</scope>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>io.goodforgod</groupId>
        <artifactId>graalvm-hint-processor</artifactId>
        <version>0.20.0</version>
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
            <version>3.10.1</version>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>io.goodforgod</groupId>
                        <artifactId>graalvm-hint-processor</artifactId>
                        <version>0.20.0</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Content

* [@ReflectionHint](#reflectionhint)
    + [Self Config](#reflection-self-config)
    + [Multi Config](#reflection-multi-config)
* [@ResourceHint](#resourcehint)
    + [Include Patterns](#include-patterns)
    + [Exclude Patterns](#exclude-patterns)
    + [Include Bundles](#include-bundles)
* [@NativeImageHint](#nativeimagehint)
    + [Entrypoint](#entrypoint)
    + [Entrypoint & Options](#entrypoint-and-options)
* [@InitializationHint](#initializationhint)
    + [Runtime & Compile Time](#runtime-and-compile-time-config)
    + [Self Config](#initialization-self-config)
* [@DynamicProxyHint](#dynamicproxyhint)
    + [Resources & Files](#resources-and-files-config)
    + [Interfaces Multi Config](#interfaces-multi-config)
    + [Interfaces Self Config](#interfaces-self-config)
* [@JniHint](#jnihint)
    + [JNI Self Config](#jni-self-config)
    + [JNI Multi Config](#jni-multi-config)
* [@LinkHint](#linkhint)
  + [Link Self Config](#link-self-config)
  + [Link Multi Config](#link-multi-config)
  + [Link All Classes Config](#link-all-classes-config)
* [Group & Artifact name](#group-and-artifact-name)

## @ReflectionHint

You can read more about GraalVM reflection configuration [in official documentation here](https://www.graalvm.org/reference-manual/native-image/Reflection/).

There are available access hints:
- allPublicFields
- allPublicMethods
- allPublicConstructors
- allDeclaredFields
- allDeclaredMethods
- allDeclaredConstructors

Generating reflection access, most used cases is DTOs that are used for serialization/deserialization in any format (JSON for example).

### Reflection Self Config

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

### Reflection Multi Config

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

## @ResourceHint

You can read more about GraalVM resource configuration [in official documentation here](https://www.graalvm.org/reference-manual/native-image/Resources/).

Hint allows generating config for resource files to be included/excluded when building native application.
You can also include bundles into native image using this Hint.

### Include Patterns

Resource patterns specified with Java regexp to Include during native-image generation into the final application.

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

### Exclude Patterns

Resource patterns specified with Java regexp to Exclude during native-image generation into the final application.

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

### Include Bundles

Native Image needs ahead-of-time knowledge of the resource bundles your application needs so that it can load and store the appropriate bundles for usage in the generated binary.

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

## @NativeImageHint

You can read more about GraalVM native-image options [in official documentation here](https://www.graalvm.org/reference-manual/native-image/Options/).

Hint allows generating config for native-image options and initial application entrypoint.

### Entrypoint

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

### Entrypoint and Options

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

## @InitializationHint

You can read more about GraalVM initialization configuration [in official documentation here](https://www.graalvm.org/reference-manual/native-image/ClassInitialization/).

Hint allows generating config for what classes to instantiate in runtime and what classes to instantiate in compile time.

### Runtime and Compile Time Config

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

### Initialization Self Config

Simple case for single Java class:
```java
@InitializationHint
public class Self {

}
```

Generated *native-image.properties*:
```properties
Args = --initialize-at-build-time=io.goodforgod.graalvm.hint.processor.Self
```

## @DynamicProxyHint

You can read more about GraalVM DynamicProxyHint configuration [in official documentation here](https://www.graalvm.org/reference-manual/native-image/DynamicProxy/).

### Resources and Files Config

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

### Interfaces Multi Config

You can fully configure proxy yourself using annotations only, without the need for manually creating JSON configurations.
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

### Interfaces Self Config

In case you need to add only one interface for DynamicProxy Hint configuration, you can annotate that interface directly:
```java
@DynamicProxyHint
public interface Self {

}
```

Generated *dynamic-proxy-hint-config.json*:
```json
[
  { "interfaces": [ "io.goodforgod.graalvm.hint.processor.Self" ] }
]
```

Generated *native-image.properties*:
```properties
Args = -H:DynamicProxyConfigurationResources=META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/dynamic-proxy-config.json
```

## @JniHint

You can read more about GraalVM JNI configuration [in official documentation here](https://www.graalvm.org/reference-manual/native-image/JNI/).

There are available JNI access hints:
- allPublicFields
- allPublicMethods
- allPublicConstructors
- allDeclaredFields
- allDeclaredMethods
- allDeclaredConstructors

### JNI Self Config

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

### JNI Multi Config

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

## @LinkHint

You can read more about GraalVM Link Build configuration [in official documentation here](https://www.graalvm.org/22.1/reference-manual/native-image/Options/#options-to-native-image-builder).

Hint is only **available in GraalVM 22.1.0+**, check this [PR for more info](https://github.com/oracle/graal/pull/4305).

Specify types to be fully defined at image build-time.

### Link Self Config

Simple case for single Java class:
```java
@LinkHint
public class RequestOnly {

    private String name;
}
```

Generated *native-image.properties*:
```properties
Args = --link-at-build-time=io.goodforgod.graalvm.hint.processor.RequestOnly
```

### Link Multi Config

There may be more different cases, like generating hints for classes that are package private or just private, also there hint can be used for whole package.

Complex example generating link hints:
```java
@LinkHint(types = { Response.class, Request.class })
@LinkHint(typeNames = { "io.goodforgod.graalvm.hint.processor"})
public final class JniConfig {

}
```

Generated *native-image.properties*:
```properties
Args = --link-at-build-time=io.goodforgod.graalvm.hint.processor,io.goodforgod.graalvm.hint.processor.Response,io.goodforgod.graalvm.hint.processor.Request
```

### Link All Classes Config

Is the same as using GraalVM flag without options.
```text
If used without args, all classes in scope of the option are required to be fully defined.
```

Example how to force all classes to link in build time, no matter what how other class declare this hint, if any annotation with all = true found, then it will be used this way.
```java
@LinkHint(all = true)
public class RequestOnly {

    private String name;
}
```

Generated *native-image.properties*:
```properties
Args = --link-at-build-time
```

## Group and Artifact name

You can change the output group and artifact name, by default the *group* will be the package name where the annotated class was located and the artifact will be named *hint*.

For class:
```java
package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.ReflectionHint;

@ReflectionHint
public class Request {

    private String name;
}
```

Hint will be generated into `build/classes/java/main/META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/reflect-config.json` directory.
Where *io.goodforgod.graalvm.hint.processor* is *group* name and *hint* artifact name.

You can override default behavior and select our own *group* and *artifact* name via annotation [processor options](https://docs.oracle.com/javase/7/docs/technotes/tools/windows/javac.html).

Annotation processor options:
- *graalvm.hint.group* - group name.
- *graalvm.hint.artifact* - artifact name.

Here are examples of configurations for Gradle and Maven.

**Gradle**
```groovy
compileJava {
    options.compilerArgs += [
            "-Agraalvm.hint.group=my.group",
            "-Agraalvm.hint.artifact=myartifact",
    ]
}
```

**Maven**
```xml
<build>
  <plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.10.1</version>
        <configuration>
            <parameters>true</parameters>
            <compilerArgs>
                <compilerArg>-Agraalvm.hint.group=my.group</compilerArg>
                <compilerArg>-Agraalvm.hint.artifact=myartifact</compilerArg>
            </compilerArgs>
        </configuration>
    </plugin>
  </plugins>
</build>
```

## License

This project licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details
