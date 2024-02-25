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
 * @since 21.03.2022
 */
class JniHintProcessorTests extends ProcessorRunner {

    @Test
    void jniHintSelf() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new JniHintProcessor())
                .compile(JavaFileObjects.forResource("jnihint/source/RequestOnly.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/jni-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("jnihint/generated/jni-config-only.json"));
    }

    @Test
    void jniHintForMultipleAnnotations() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new JniHintProcessor())
                .compile(JavaFileObjects.forResource("jnihint/source/Response.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/jni-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("jnihint/generated/jni-config.json"));
    }

    @Test
    void jniHintForMultipleClassesAndMultipleAnnotations() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new JniHintProcessor())
                .compile(JavaFileObjects.forResource("jnihint/source/RequestOnly.java"),
                        JavaFileObjects.forResource("jnihint/source/Response.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/jni-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("jnihint/generated/jni-config-many.json"));
    }

    @Test
    void jniHintForMultipleAccessHints() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new JniHintProcessor())
                .compile(JavaFileObjects.forResource("jnihint/source/RequestOnlyManyAccess.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/jni-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("jnihint/generated/jni-config-only-many-access.json"));
    }
}
