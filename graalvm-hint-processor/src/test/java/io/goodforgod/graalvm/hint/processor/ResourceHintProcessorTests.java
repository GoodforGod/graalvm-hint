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
class ResourceHintProcessorTests extends ProcessorRunner {

    @Test
    void resourceHintForClassSuccess() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ResourceHintProcessor())
                .compile(JavaFileObjects.forResource("resourcehint/source/ResourceNames.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint/processor/resource-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("resourcehint/generated/resource-config-single.json"));
    }

    @Test
    void resourceHintForMultipleClassesSuccess() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ResourceHintProcessor())
                .compile(JavaFileObjects.forResource("resourcehint/source/ResourceNames.java"),
                        JavaFileObjects.forResource("resourcehint/source/ResourcePatterns.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint/processor/resource-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("resourcehint/generated/resource-config.json"));
    }
}
