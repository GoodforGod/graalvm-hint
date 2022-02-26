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
class NativeImageHintProcessorTests extends ProcessorRunner {

    @Test
    void nativeImageHintEntrypointOnlySuccess() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("nativeimagehint/source/EntrypointOnly.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint/processor/native-image.properties")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("nativeimagehint/generated/native-image-only.properties"));
    }

    @Test
    void nativeImageHintMultipleClassesMultipleAnnotations() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("nativeimagehint/source/Entrypoint.java"),
                        JavaFileObjects.forResource("nativeimagehint/source/EntrypointOptions.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint/processor/native-image.properties")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("nativeimagehint/generated/native-image.properties"));
    }
}
