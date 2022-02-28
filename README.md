# GraalVM Hint Processor

![GraalVM Enabled](https://img.shields.io/badge/GraalVM-Ready-orange?style=plastic)
[![GitHub Action](https://github.com/goodforgod/slf4j-simple-logger/workflows/Java%20CI/badge.svg)](https://github.com/GoodforGod/slf4j-simple-logger/actions?query=workflow%3A%22Java+CI%22)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_slf4j-simple-logger&metric=coverage)](https://sonarcloud.io/dashboard?id=GoodforGod_slf4j-simple-logger)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_slf4j-simple-logger&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=GoodforGod_slf4j-simple-logger)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_slf4j-simple-logger&metric=ncloc)](https://sonarcloud.io/dashboard?id=GoodforGod_slf4j-simple-logger)

GraalVM Hint Processor helps generate GraalVM hints for building [native-image](https://www.graalvm.org/reference-manual/native-image/) applications.

Features:
- Generate Reflection Hints ([reflect-config.json](https://www.graalvm.org/reference-manual/native-image/Reflection/))
- Generate Resource Hints ([resource-config.json](https://www.graalvm.org/reference-manual/native-image/Resources/))
- Generate Options hints ([native-image.properties](https://www.graalvm.org/reference-manual/native-image/Options/))
- Generate Initialization hints ([native-image.properties](https://www.graalvm.org/reference-manual/native-image/ClassInitialization/))

## Dependency :rocket:

[**Gradle**](https://mvnrepository.com/artifact/io.goodforgod/io.graalvm-hint-processor)
```groovy
annotationProcessor "io.graalvm-hint-processor:0.11.0"
compilyOnly "io.graalvm-hint-annotations:0.11.0"
```

[**Maven**](https://mvnrepository.com/artifact/io.goodforgod/io.graalvm-hint-processor)
```xml
<dependency>
    <groupId>io.goodforgod</groupId>
    <artifactId>graalvm-hint-annotations</artifactId>
    <version>0.11.0</version>
    <scope>compile</scope>
    <optional>true</optional>
</dependency>
```

```xml
<pluginManagement>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.10.0</version>
            <configuration>
                <annotationProcessorPaths>
                    <annotationProcessorPath>
                        <groupId>io.goodforgod</groupId>
                        <artifactId>graalvm-hint-processor</artifactId>
                        <version>0.11.0</version>
                    </annotationProcessorPath>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</pluginManagement>
```

## Examples

### TypeHint

### ResourceHint

### NativeImageHint

### InitializationHint

## License

This project licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details