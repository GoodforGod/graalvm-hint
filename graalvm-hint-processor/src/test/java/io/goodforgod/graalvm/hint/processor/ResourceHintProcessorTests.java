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
class ResourceHintProcessorTests extends ProcessorRunner {

    @Test
    void includeHint() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ResourceHintProcessor())
                .compile(JavaFileObjects.forResource("resourcehint/source/ResourceInclude.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/resource-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("resourcehint/generated/resource-config-include.json"));
    }

    @Test
    void excludeHint() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ResourceHintProcessor())
                .compile(JavaFileObjects.forResource("resourcehint/source/ResourceExclude.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/resource-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("resourcehint/generated/resource-config-exclude.json"));
    }

    @Test
    void bundleHint() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ResourceHintProcessor())
                .compile(JavaFileObjects.forResource("resourcehint/source/ResourceBundle.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/resource-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("resourcehint/generated/resource-config-bundle.json"));
    }

    @Test
    void emptyHint() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ResourceHintProcessor())
                .compile(JavaFileObjects.forResource("resourcehint/source/ResourceEmpty.java"));

        CompilationSubject.assertThat(compilation).failed();
    }

    @Test
    void includeAndExcludeMultipleFilesHint() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ResourceHintProcessor())
                .compile(JavaFileObjects.forResource("resourcehint/source/ResourceInclude.java"),
                        JavaFileObjects.forResource("resourcehint/source/ResourceExclude.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/resource-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("resourcehint/generated/resource-config-include-and-exclude.json"));
    }

    @Test
    void includeAndBundleMultipleFilesHint() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ResourceHintProcessor())
                .compile(JavaFileObjects.forResource("resourcehint/source/ResourceInclude.java"),
                        JavaFileObjects.forResource("resourcehint/source/ResourceBundle.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/resource-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("resourcehint/generated/resource-config-include-and-bundle.json"));
    }

    @Test
    void excludeAndBundleMultipleFilesHint() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ResourceHintProcessor())
                .compile(JavaFileObjects.forResource("resourcehint/source/ResourceExclude.java"),
                        JavaFileObjects.forResource("resourcehint/source/ResourceBundle.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/resource-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("resourcehint/generated/resource-config-exclude-and-bundle.json"));
    }

    @Test
    void includeAndExcludeAndBundleOneFileHint() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ResourceHintProcessor())
                .compile(JavaFileObjects.forResource("resourcehint/source/ResourceAll.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/resource-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("resourcehint/generated/resource-config-all.json"));
    }

    @Test
    void includeAndExcludeAndBundleMultipleFilesHint() {
        final Compilation compilation = Compiler.javac()
                .withProcessors(new ResourceHintProcessor())
                .compile(JavaFileObjects.forResource("resourcehint/source/ResourceInclude.java"),
                        JavaFileObjects.forResource("resourcehint/source/ResourceExclude.java"),
                        JavaFileObjects.forResource("resourcehint/source/ResourceBundle.java"));

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT,
                        "META-INF/native-image/io.goodforgod.graalvm.hint.processor/hint/resource-config.json")
                .contentsAsString(StandardCharsets.UTF_8)
                .isEqualTo(getResourceContentAsString("resourcehint/generated/resource-config-all.json"));
    }
}
