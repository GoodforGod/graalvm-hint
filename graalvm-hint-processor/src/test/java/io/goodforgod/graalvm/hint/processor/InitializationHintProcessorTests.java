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
class InitializationHintProcessorTests extends ProcessorRunner {

    @Test
    void initializationHintBuildAndRuntime() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("initializationhint/source/BuildAndRuntime.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint/processor/native-image.properties")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("initializationhint/generated/native-image-build-runtime.properties"));
    }

    @Test
    void initializationHintBuild() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("initializationhint/source/Build.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint/processor/native-image.properties")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("initializationhint/generated/native-image-build.properties"));
    }

    @Test
    void initializationHintEntrypointAndBuildAndRuntime() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new NativeImageHintProcessor())
                .compile(JavaFileObjects.forResource("initializationhint/source/EntrypointAndBuildAndRuntime.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint/processor/native-image.properties")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString(
                        "initializationhint/generated/native-image-entrypoint-build-runtime.properties"));
    }
}
