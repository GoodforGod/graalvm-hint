package io.goodforgod.graalvm.hint.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import java.nio.charset.StandardCharsets;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.Test;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 25.10.2021
 */
class DynamicProxyHintProcessorTests extends ProcessorRunner {

    @Test
    void selfHint() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("dynamicproxyhint/source/Self.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/native-image.properties")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("dynamicproxyhint/generated/native-image-config.properties"));

        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/dynamic-proxy-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("dynamicproxyhint/generated/dynamic-proxy-hint-self.json"));
    }

    @Test
    void selfClassHintFail() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("dynamicproxyhint/source/SelfClass.java"));

        CompilationSubject.assertThat(compilation).failed();
    }

    @Test
    void configHint() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("dynamicproxyhint/source/Config.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/native-image.properties")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("dynamicproxyhint/generated/native-image-config.properties"));

        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/dynamic-proxy-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("dynamicproxyhint/generated/dynamic-proxy-hint-config.json"));
    }

    @Test
    void resourceHint() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("dynamicproxyhint/source/Resource.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/native-image.properties")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("dynamicproxyhint/generated/native-image-resource.properties"));
    }

    @Test
    void resourceAndConfigHintSingleFile() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("dynamicproxyhint/source/ResourceAndConfig.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/native-image.properties")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("dynamicproxyhint/generated/native-image-resource-and-config.properties"));

        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/dynamic-proxy-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("dynamicproxyhint/generated/dynamic-proxy-hint-resource-and-config.json"));
    }

    @Test
    void resourceAndConfigHintSeparateFiles() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("dynamicproxyhint/source/Resource.java"),
                        JavaFileObjects.forResource("dynamicproxyhint/source/Config.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/native-image.properties")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("dynamicproxyhint/generated/native-image-resource-and-config.properties"));

        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/dynamic-proxy-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("dynamicproxyhint/generated/dynamic-proxy-hint-resource-and-config.json"));
    }
}
