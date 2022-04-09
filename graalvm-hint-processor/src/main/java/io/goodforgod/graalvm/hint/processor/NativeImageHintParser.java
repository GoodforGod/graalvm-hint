package io.goodforgod.graalvm.hint.processor;

import static io.goodforgod.graalvm.hint.processor.AbstractHintProcessor.getAnnotationFieldClassNameAny;

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
        private final NativeImageHint hint;

        private Entrypoint(String className, NativeImageHint hint) {
            this.className = className;
            this.hint = hint;
        }
    }

    @Override
    public List<Class<? extends Annotation>> annotations() {
        return List.of(NativeImageHint.class);
    }

    @Override
    public List<String> getOptions(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
        final Set<? extends Element> annotatedNative = roundEnv.getElementsAnnotatedWith(NativeImageHint.class);
        final Set<TypeElement> elements = ElementFilter.typesIn(annotatedNative);

        final List<Entrypoint> entrypoints = elements.stream()
                .map(t -> getAnnotationFieldClassNameAny(t, NativeImageHint.class, "entrypoint")
                        .filter(name -> !ENTRY_POINT_DEFAULT_VALUE.equals(name))
                        .map(name -> new Entrypoint(name, t.getAnnotation(NativeImageHint.class)))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (entrypoints.size() > 1) {
            throw new IllegalStateException("@NativeImageHint multiple entrypoint detected with values: " + entrypoints);
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
        return (entrypoint.hint.name().isBlank())
                ? List.of("-H:Class=" + entrypoint.className)
                : List.of("-H:Name=" + entrypoint.hint.name() + " -H:Class=" + entrypoint.className);
    }
}
