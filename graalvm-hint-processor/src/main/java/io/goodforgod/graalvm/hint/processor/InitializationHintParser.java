package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.InitializationHint;
import io.goodforgod.graalvm.hint.annotation.InitializationHint.InitPhase;
import io.goodforgod.graalvm.hint.annotation.InitializationHints;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
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
        if (elements.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<InitPhase, List<Initialization>> groupedInitializationOptions = elements.stream()
                .flatMap(e -> {
                    final InitializationHints hints = e.getAnnotation(InitializationHints.class);
                    if (hints == null) {
                        final InitializationHint hint = e.getAnnotation(InitializationHint.class);
                        return getAnnotationPhases(e, hint).stream();
                    } else {
                        return getParentAnnotationPhases(e, InitializationHint.class, InitializationHints.class).stream();
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

    private static Collection<Initialization> getAnnotationPhases(TypeElement element,
                                                                  InitializationHint hint) {
        final InitializationHint.InitPhase phase = hint.value();
        final List<String> typeNames = Arrays.asList(hint.typeNames());
        final List<String> types = HintUtils.getAnnotationFieldClassNames(element, InitializationHint.class, "types");
        if (types.isEmpty() && typeNames.isEmpty()) {
            final String selfName = element.getQualifiedName().toString();
            return List.of(new Initialization(selfName, phase));
        }

        return Stream.concat(types.stream(), typeNames.stream())
                .map(t -> new Initialization(t, phase))
                .collect(Collectors.toList());
    }

    private static List<Initialization> getParentAnnotationPhases(TypeElement type,
                                                                  Class<? extends Annotation> annotation,
                                                                  Class<? extends Annotation> parentAnnotation) {
        final String annotationName = annotation.getSimpleName();
        final String annotationParent = parentAnnotation.getSimpleName();
        return type.getAnnotationMirrors().stream()
                .filter(pa -> pa.getAnnotationType().asElement().getSimpleName().contentEquals(annotationParent))
                .flatMap(pa -> pa.getElementValues().entrySet().stream())
                .flatMap(e -> ((List<?>) e.getValue().getValue()).stream().map(AnnotationMirror.class::cast))
                .filter(a -> (a.getAnnotationType().asElement().getSimpleName().contentEquals(annotationName)))
                .flatMap(a -> {
                    final List<String> types = HintUtils.getAnnotationFieldValues(a, "types");
                    final List<String> typeNames = HintUtils.getAnnotationFieldValues(a, "typeNames");
                    final InitializationHint.InitPhase phase = HintUtils
                            .getAnnotationFieldValues(a, "value")
                            .stream()
                            .map(InitializationHint.InitPhase::valueOf)
                            .findFirst()
                            .orElse(InitPhase.BUILD);

                    return (types.isEmpty() && typeNames.isEmpty())
                            ? Stream.empty()
                            : Stream.concat(types.stream(), typeNames.stream()).map(t -> new Initialization(t, phase));
                })
                .collect(Collectors.toList());
    }

    private String getInitializationArgumentName(InitPhase phase) {
        return (InitPhase.BUILD.equals(phase))
                ? INIT_BUILD_TIME
                : INIT_RUNTIME_TIME;
    }
}
