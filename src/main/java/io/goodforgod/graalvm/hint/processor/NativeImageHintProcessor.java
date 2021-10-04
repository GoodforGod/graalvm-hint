package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.InitializationHint;
import io.goodforgod.graalvm.hint.annotation.NativeImageHint;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/**
 * Please Add Description Here.
 *
 * @author Anton Kurako (GoodforGod)
 * @see NativeImageHint
 * @see InitializationHint
 * @since 30.09.2021
 */
@SupportedAnnotationTypes({
        "io.graalvm.hint.annotation.NativeImageHint",
        "io.graalvm.hint.annotation.InitializationHint"
})
@SupportedOptions({
        HintOptions.HINT_PROCESSING_GROUP,
        HintOptions.HINT_PROCESSING_ARTIFACT
})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class NativeImageHintProcessor extends AbstractHintProcessor {

    private static final String FILE_NAME = "native-image.properties";
    private static final String ARG_SEPARATOR = " \\\n       ";

    static class Entrypoint {

        private final String className;
        private final NativeImageHint hint;

        private Entrypoint(String className, NativeImageHint hint) {
            this.className = className;
            this.hint = hint;
        }

        @Override
        public String toString() {
            return className;
        }
    }

    static class Initialization {

        private final InitializationHint.InitPhase phase;
        private final String className;

        Initialization(InitializationHint.InitPhase phase, String className) {
            this.phase = phase;
            this.className = className;
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        final Set<? extends Element> annotatedNative = roundEnv.getElementsAnnotatedWith(NativeImageHint.class);
        final Set<TypeElement> typesNative = ElementFilter.typesIn(annotatedNative);
        final List<String> nativeImageHintOptions = getNativeImageHintProperties(typesNative);

        final Set<? extends Element> annotatedInits = roundEnv.getElementsAnnotatedWith(InitializationHint.class);
        final Set<TypeElement> typesInits = ElementFilter.typesIn(annotatedInits);
        final List<String> initializationHintOptions = getInitializationHintProperties(typesInits);

        final String nativeImageProperties = Stream.of(nativeImageHintOptions, initializationHintOptions)
                .flatMap(Collection::stream)
                .collect(Collectors.joining(ARG_SEPARATOR, "Args = ", ""));

        if (nativeImageProperties.isEmpty() && initializationHintOptions.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING,
                    typesNative.size() + " annotations @NativeImageHint are present but not Options or Entrypoint found!");
            return false;
        } else {
            return writeConfigFile(FILE_NAME, nativeImageProperties, roundEnv);
        }
    }

    private List<String> getNativeImageHintProperties(Set<TypeElement> elements) {
        final List<Entrypoint> entrypoints = elements.stream()
                .map(t -> getAnnotationFieldClassNameAny(t, NativeImageHint.class, "entrypoint")
                        .filter(v -> !v.equals(NativeImageHint.class.getSimpleName()))
                        .map(v -> new Entrypoint(v, t.getAnnotation(NativeImageHint.class)))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (entrypoints.size() > 1) {
            throw new IllegalStateException("@NativeImageHint multiple entrypoint detected with values: " + entrypoints);
        }

        final Optional<Entrypoint> entryClassName = entrypoints.stream().findFirst();
        final List<String> options = elements.stream()
                .map(t -> t.getAnnotation(NativeImageHint.class))
                .flatMap(t -> Arrays.stream(t.options()))
                .distinct()
                .collect(Collectors.toList());

        return entryClassName
                .map(entry -> {
                    final List<String> entryOptions = List.of("-H:Name=" + entry.hint.name() + " -H:Class=" + entry.className);
                    return Stream.of(entryOptions, options)
                            .flatMap(Collection::stream)
                            .distinct()
                            .collect(Collectors.toList());
                })
                .orElse(options);
    }

    private List<String> getInitializationHintProperties(Set<TypeElement> elements) {

        final Map<InitializationHint.InitPhase, List<Initialization>> groupedInitializationOptions = elements.stream()
                .flatMap(e -> {
                    final InitializationHint annotation = e.getAnnotation(InitializationHint.class);
                    final List<String> types = getAnnotationFieldClassNames(e, InitializationHint.class, "types");
                    final List<String> typeNames = Arrays.asList(annotation.typeNames());
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING,
                            "--- types " + types + ", names " + typeNames);
                    if (types.isEmpty() && typeNames.isEmpty()) {
                        return Stream.of(new Initialization(annotation.value(), e.getQualifiedName().toString()));
                    } else {
                        return Stream.of(types, typeNames)
                                .flatMap(Collection::stream)
                                .map(type -> new Initialization(annotation.value(), type));
                    }
                })
                .collect(Collectors.groupingBy(e -> e.phase));

        return groupedInitializationOptions.entrySet().stream()
                .map(e -> e.getValue().stream()
                        .map(i -> i.className)
                        .collect(Collectors.joining(",", getInitializationArgumentName(e.getKey()), "")))
                .collect(Collectors.toList());
    }

    private String getInitializationArgumentName(InitializationHint.InitPhase phase) {
        return (InitializationHint.InitPhase.BUILD.equals(phase))
                ? "--initialize-at-build-time="
                : "--initialize-at-run-time=";
    }
}
