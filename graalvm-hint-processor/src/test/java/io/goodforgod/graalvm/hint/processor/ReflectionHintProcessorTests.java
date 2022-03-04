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
class ReflectionHintProcessorTests extends ProcessorRunner {

    @Test
    void reflectionHintForSelfClassSuccess() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ReflectionHintProcessor())
                .compile(JavaFileObjects.forResource("reflectionhint/source/RequestOnly.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint/processor/reflect-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("reflectionhint/generated/reflect-config-only.json"));
    }

    @Test
    void reflectionHintForMultipleClassesAndMultipleAnnotationsSuccess() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ReflectionHintProcessor())
                .compile(JavaFileObjects.forResource("reflectionhint/source/RequestOnly.java"),
                        JavaFileObjects.forResource("reflectionhint/source/Response.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint/processor/reflect-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("reflectionhint/generated/reflect-config.json"));
    }

    @Test
    void reflectionHintForMultipleAccessHintsSuccess() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ReflectionHintProcessor())
                .compile(JavaFileObjects.forResource("reflectionhint/source/RequestOnlyManyAccess.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint/processor/reflect-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("reflectionhint/generated/reflect-config-only-many-access.json"));
    }
}
