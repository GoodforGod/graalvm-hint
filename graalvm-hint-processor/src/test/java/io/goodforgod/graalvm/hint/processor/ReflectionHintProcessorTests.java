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
class ReflectionHintProcessorTests extends ProcessorRunner {

    @Test
    void reflectionHintSelf() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ReflectionHintProcessor())
                .compile(JavaFileObjects.forResource("reflectionhint/source/RequestOnly.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/reflect-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("reflectionhint/generated/reflect-config-only.json"));
    }

    @Test
    void reflectionHintForMultipleAnnotations() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ReflectionHintProcessor())
                .compile(JavaFileObjects.forResource("reflectionhint/source/Response.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/reflect-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("reflectionhint/generated/reflect-config.json"));
    }

    @Test
    void reflectionHintForMultipleClassesAndMultipleAnnotations() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ReflectionHintProcessor())
                .compile(JavaFileObjects.forResource("reflectionhint/source/RequestOnly.java"),
                        JavaFileObjects.forResource("reflectionhint/source/Response.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/reflect-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("reflectionhint/generated/reflect-config-many.json"));
    }

    @Test
    void reflectionHintForMultipleClassesAndManyAnnotations() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ReflectionHintProcessor())
                .compile(JavaFileObjects.forResource("reflectionhint/source/RequestOnly.java"),
                        JavaFileObjects.forResource("reflectionhint/source/Multiple.java"),
                        JavaFileObjects.forResource("reflectionhint/source/ResponseOnly.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/reflect-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("reflectionhint/generated/reflect-config-all.json"));
    }

    @Test
    void reflectionHintForMultipleAccessHints() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ReflectionHintProcessor())
                .compile(JavaFileObjects.forResource("reflectionhint/source/RequestOnlyManyAccess.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/reflect-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("reflectionhint/generated/reflect-config-only-many-access.json"));
    }

    @Test
    void reflectionHintForInnerClass() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ReflectionHintProcessor())
                .compile(JavaFileObjects.forResource("reflectionhint/source/InnerClass.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/reflect-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("reflectionhint/generated/reflect-config-inner-class.json"));
    }

    @Test
    void reflectionHintForInnerClassSelf() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ReflectionHintProcessor())
                .compile(JavaFileObjects.forResource("reflectionhint/source/InnerSelf.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/reflect-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("reflectionhint/generated/reflect-config-inner-self.json"));
    }

    @Test
    void reflectionHintForInnerInnerClass() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ReflectionHintProcessor())
                .compile(JavaFileObjects.forResource("reflectionhint/source/InnerInnerClass.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/reflect-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("reflectionhint/generated/reflect-config-inner-inner-class.json"));
    }

    @Test
    void reflectionHintForInnerInnerClassSelf() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ReflectionHintProcessor())
                .compile(JavaFileObjects.forResource("reflectionhint/source/InnerInnerSelf.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/reflect-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("reflectionhint/generated/reflect-config-inner-inner-self.json"));
    }
}
