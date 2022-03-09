package io.goodforgod.graalvm.hint.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import java.nio.charset.StandardCharsets;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.Test;

/**
 * @author GoodforGod
 * @since 13.11.2019
 */
class InitializationHintProcessorTests extends ProcessorRunner {

    @Test
    void nativeImageHintEntrypointOnlySuccess() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("initializationhint/source/EntrypointOnly.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint/processor/native-image.properties")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("initializationhint/generated/native-image-only.properties"));
    }

    @Test
    void nativeImageHintMultipleClassesMultipleAnnotations() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("initializationhint/source/Entrypoint.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint/processor/native-image.properties")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("initializationhint/generated/native-image.properties"));
    }
}
