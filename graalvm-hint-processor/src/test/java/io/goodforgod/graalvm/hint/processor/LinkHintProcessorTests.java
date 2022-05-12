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
class LinkHintProcessorTests extends ProcessorRunner {

    @Test
    void selfConfig() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("linkhint/source/Self.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/native-image.properties")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("linkhint/generated/native-image-self.properties"));
    }

    @Test
    void typeAndTypename() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("linkhint/source/TypeAndTypename.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/native-image.properties")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("linkhint/generated/native-image-type-typename.properties"));
    }

    @Test
    void initializationHintBuild() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("linkhint/source/Type.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/native-image.properties")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("linkhint/generated/native-image-type.properties"));
    }

    @Test
    void initializationHintEntrypointAndBuildAndRuntime() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("linkhint/source/EntrypointAndLink.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/native-image.properties")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString(
                        "linkhint/generated/native-image-entrypoint-link.properties"));
    }
}
