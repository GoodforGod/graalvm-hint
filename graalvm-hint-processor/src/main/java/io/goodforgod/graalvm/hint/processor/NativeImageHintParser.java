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
 * @author Anton Kurako (GoodforGod)
 * @see NativeImageHint
 * @since 07.04.2022
 */
final class NativeImageHintParser implements OptionParser {

    private static final String ENTRY_POINT_DEFAULT_VALUE = Void.class.getSimpleName();

    private static class Entrypoint {

        private final String className;
        private final TypeElement source;
        private final HintOrigin origin;

        private Entrypoint(String className, TypeElement source, HintOrigin origin) {
            this.className = className;
            this.source = source;
            this.origin = origin;
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
    public List<Option> getOptions(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
        final Set<? extends Element> annotatedNative = roundEnv.getElementsAnnotatedWith(NativeImageHint.class);
        final Set<TypeElement> elements = ElementFilter.typesIn(annotatedNative);
        if (elements.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Entrypoint> entrypoints = elements.stream()
                .map(element -> HintUtils.getAnnotationFieldClassNameAny(element, NativeImageHint.class, "entrypoint")
                        .filter(name -> !ENTRY_POINT_DEFAULT_VALUE.equals(name))
                        .map(name -> new Entrypoint(name, element, HintUtils.getHintOrigin(element, processingEnv)))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (entrypoints.size() > 1) {
            throw new HintException("@NativeImageHint multiple entrypoint detected: " + entrypoints, entrypoints.get(1).source);
        }

        final Map<HintOrigin, List<String>> options = new HashMap<>();
        entrypoints.stream().findFirst().ifPresent(entrypoint -> {
            final List<String> entryOptions = getEntrypointOptions(entrypoint);
            options.put(entrypoint.origin, new ArrayList<>(entryOptions));
        });

        for (TypeElement element : elements) {
            final NativeImageHint hint = element.getAnnotation(NativeImageHint.class);
            final List<String> hintOptions = Stream
                    .concat(Arrays.stream(hint.options()).map(NativeImageOptions::option), Arrays.stream(hint.optionNames()))
                    .distinct()
                    .collect(Collectors.toList());

            if (!hintOptions.isEmpty()) {
                final HintOrigin origin = HintUtils.getHintOrigin(element, processingEnv);
                final List<String> resultOptions = options.computeIfAbsent(origin, (k) -> new ArrayList<>());
                resultOptions.addAll(hintOptions);
            }
        }

        return options.entrySet().stream()
                .map(e -> new Option(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<String> getEntrypointOptions(Entrypoint entrypoint) {
        final String appName = entrypoint.hint().name();
        return (appName.isBlank())
                ? List.of("-H:Class=" + entrypoint.className)
                : List.of("-H:Class=" + entrypoint.className + " -H:Name=" + appName);
    }
}
