package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.NativeImageHint;
import io.goodforgod.graalvm.hint.annotation.NativeImageOptions;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

/**
 * Processes {@link NativeImageHint} annotations for native-image.properties file
 *
 * @author Anton Kurako (GoodforGod)
 * @see NativeImageHint
 * @author Anton Kurako (GoodforGod)
 * @since 07.04.2022
 */
final class NativeImageHintParser implements OptionParser {

    private static final String ENTRY_POINT_DEFAULT_VALUE = Void.class.getSimpleName();

    private static class Entrypoint {

        private final String className;
        private final TypeElement source;

        private Entrypoint(String className, TypeElement source) {
            this.className = className;
            this.source = source;
        }

        NativeImageHint hint() {
            return source.getAnnotation(NativeImageHint.class);
        }

        @Override
        public String toString() {
            return "entrypoint=" + className + ", source=" + source.getQualifiedName();
        }
    }

    @Override
    public List<Class<? extends Annotation>> getSupportedAnnotations() {
        return List.of(NativeImageHint.class);
    }

    @Override
    public List<String> getOptions(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
        final Set<? extends Element> annotatedNative = roundEnv.getElementsAnnotatedWith(NativeImageHint.class);
        final Set<TypeElement> elements = ElementFilter.typesIn(annotatedNative);
        if (elements.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Entrypoint> entrypoints = elements.stream()
                .map(element -> HintUtils.getAnnotationFieldClassNameAny(element, NativeImageHint.class, "entrypoint")
                        .filter(name -> !ENTRY_POINT_DEFAULT_VALUE.equals(name))
                        .map(name -> new Entrypoint(name, element))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (entrypoints.size() > 1) {
            throw new HintException("@NativeImageHint multiple entrypoints detected: " + entrypoints);
        }

        final Optional<Entrypoint> entryClassName = entrypoints.stream().findFirst();
        final List<String> options = elements.stream()
                .map(element -> element.getAnnotation(NativeImageHint.class))
                .flatMap(hint -> Stream
                        .concat(Arrays.stream(hint.options()).map(NativeImageOptions::option), Arrays.stream(hint.optionNames()))
                        .distinct())
                .collect(Collectors.toList());

        return entryClassName
                .map(entry -> {
                    final List<String> entryOptions = getEntrypointOptions(entry);
                    return Stream.of(entryOptions, options)
                            .flatMap(Collection::stream)
                            .distinct()
                            .collect(Collectors.toList());
                })
                .orElse(options);
    }

    private List<String> getEntrypointOptions(Entrypoint entrypoint) {
        final String appName = entrypoint.hint().name();
        return (appName.isBlank())
                ? List.of("-H:Class=" + entrypoint.className)
                : List.of("-H:Class=" + entrypoint.className + " -H:Name=" + appName);
    }
}
