package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.InitializationHint;
import io.goodforgod.graalvm.hint.annotation.InitializationHint.InitPhase;
import io.goodforgod.graalvm.hint.annotation.InitializationHints;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;

/**
 * Processes {@link InitializationHint} annotations for native-image.properties file
 *
 * @author Anton Kurako (GoodforGod)
 * @see InitializationHint
 * @author Anton Kurako (GoodforGod)
 * @since 07.04.2022
 */
final class InitializationHintParser implements OptionParser {

    private static final String INIT_BUILD_TIME = "--initialize-at-build-time=";
    private static final String INIT_RUNTIME_TIME = "--initialize-at-run-time=";

    private static class Initialization implements Comparable<Initialization> {

        private final String className;
        private final InitPhase phase;

        Initialization(String className, InitPhase phase) {
            this.className = className;
            this.phase = phase;
        }

        @Override
        public int compareTo(Initialization o) {
            return className.compareTo(o.className);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Initialization that = (Initialization) o;
            return Objects.equals(className, that.className);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className);
        }
    }

    @Override
    public List<Class<? extends Annotation>> getSupportedAnnotations() {
        return List.of(InitializationHint.class, InitializationHints.class);
    }

    @Override
    public List<String> getOptions(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
        final Set<TypeElement> elements = HintUtils.getAnnotatedElements(roundEnv, InitializationHint.class,
                InitializationHints.class);

        final Map<InitPhase, List<Initialization>> groupedInitializationOptions = elements.stream()
                .flatMap(e -> {
                    final InitializationHints hints = e.getAnnotation(InitializationHints.class);
                    if (hints == null) {
                        final InitializationHint hint = e.getAnnotation(InitializationHint.class);
                        return getInitializations(e, hint, false);
                    } else {
                        return Arrays.stream(hints.value())
                                .flatMap(h -> getInitializations(e, h, true));
                    }
                })
                .distinct()
                .collect(Collectors.groupingBy(e -> e.phase));

        return groupedInitializationOptions.entrySet().stream()
                .map(e -> e.getValue().stream()
                        .sorted()
                        .map(i -> i.className)
                        .collect(Collectors.joining(",", getInitializationArgumentName(e.getKey()), "")))
                .sorted()
                .collect(Collectors.toList());
    }

    private Stream<Initialization> getInitializations(TypeElement element, InitializationHint hint, boolean isParentAnnotation) {
        final List<String> types = (!isParentAnnotation)
                ? HintUtils.getAnnotationFieldClassNames(element, InitializationHint.class, "types")
                : HintUtils.getAnnotationFieldClassNames(element, InitializationHint.class, "types", InitializationHints.class,
                        getParentAnnotationPredicate(hint.value()));

        final List<String> typeNames = Arrays.asList(hint.typeNames());
        if (types.isEmpty() && typeNames.isEmpty()) {
            return Stream.of(new Initialization(element.getQualifiedName().toString(), hint.value()));
        } else {
            final Stream<String> typeStream = types.stream().map(c -> c.endsWith(".class")
                    ? c.substring(0, c.length() - 6)
                    : c);

            return Stream.concat(typeStream, typeNames.stream())
                    .map(type -> new Initialization(type, hint.value()));
        }
    }

    private Predicate<AnnotationValue> getParentAnnotationPredicate(InitPhase initPhase) {
        return a -> {
            final InitPhase annotationInitPhase = ((AnnotationMirror) a).getElementValues().entrySet().stream()
                    .filter(e -> e.getKey().getSimpleName().contentEquals("value"))
                    .findFirst()
                    .map(entry -> InitPhase.valueOf(entry.getValue().getValue().toString()))
                    .orElse(InitPhase.BUILD);

            return annotationInitPhase.equals(initPhase);
        };
    }

    private String getInitializationArgumentName(InitPhase phase) {
        return (InitPhase.BUILD.equals(phase))
                ? INIT_BUILD_TIME
                : INIT_RUNTIME_TIME;
    }
}
