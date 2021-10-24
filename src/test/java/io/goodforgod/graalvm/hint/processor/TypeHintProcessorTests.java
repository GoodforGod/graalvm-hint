package io.goodforgod.graalvm.hint.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.nio.charset.StandardCharsets;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.Test;

/**
 * @author GoodforGod
 * @since 13.11.2019
 */
class TypeHintProcessorTests extends ProcessorRunner {

    @Test
    void typeHintForSelfClassSuccess() {
        final Compilation compilation = javac()
                .withProcessors(new TypeHintProcessor())
                .compile(JavaFileObjects.forResource("typehint/source/RequestOnly.java"));

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint/processor/reflect-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("typehint/generated/reflect-config-only.json"));
    }

    @Test
    void typeHintForMultipleClassesAndMultipleAnnotationsSuccess() {
        final Compilation compilation = javac()
                .withProcessors(new TypeHintProcessor())
                .compile(JavaFileObjects.forResource("typehint/source/RequestOnly.java"),
                        JavaFileObjects.forResource("typehint/source/Response.java"));

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint/processor/reflect-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("typehint/generated/reflect-config.json"));
    }
}
