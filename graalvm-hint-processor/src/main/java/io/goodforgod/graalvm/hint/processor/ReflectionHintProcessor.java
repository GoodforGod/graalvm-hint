package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.ReflectionHint;
import io.goodforgod.graalvm.hint.annotation.ReflectionHint.AccessType;
import io.goodforgod.graalvm.hint.annotation.ReflectionHints;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

/**
 * Processes {@link ReflectionHint} annotation for native-image reflect-config.json file
 *
 * @author Anton Kurako (GoodforGod)
 * @see ReflectionHint
 * @since 27.09.2021
 */
public final class ReflectionHintProcessor extends AbstractAccessHintProcessor {

    @Override
    protected Set<Class<? extends Annotation>> getSupportedAnnotations() {
        return Set.of(ReflectionHint.class, ReflectionHints.class);
    }

    @Override
    protected String getFileName() {
        return "reflect-config.json";
    }

    @Override
    protected Collection<Access> getGraalAccessForAnnotatedElement(TypeElement element) {
        final ReflectionHints hints = element.getAnnotation(ReflectionHints.class);
        if (hints == null) {
            final ReflectionHint hint = element.getAnnotation(ReflectionHint.class);
            return getAnnotationAccesses(element, hint);
        } else {
            return getParentAnnotationAccesses(element, ReflectionHint.class, ReflectionHints.class);
        }
    }

    private static Collection<Access> getAnnotationAccesses(TypeElement element,
                                                            ReflectionHint hint) {
        final AccessType[] accessTypes = hint.value();
        final List<String> typeNames = Arrays.asList(hint.typeNames());
        final List<String> types = HintUtils.getAnnotationFieldClassNames(element, ReflectionHint.class, "types");
        if (types.isEmpty() && typeNames.isEmpty()) {
            final String selfName = HintUtils.getElementClassName(element);
            return List.of(new Access(selfName, accessTypes));
        }

        return Stream.concat(types.stream(), typeNames.stream())
                .map(t -> new Access(t, accessTypes))
                .collect(Collectors.toList());
    }

    private static List<Access> getParentAnnotationAccesses(TypeElement type,
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
                    final AccessType[] accessTypes = HintUtils
                            .getAnnotationFieldValuesOrDefault(a, "value", List.of(AccessType.ALL_DECLARED.name()))
                            .stream()
                            .map(AccessType::valueOf)
                            .toArray(AccessType[]::new);

                    return (types.isEmpty() && typeNames.isEmpty())
                            ? Stream.of(new Access(HintUtils.getElementClassName(type), accessTypes))
                            : Stream.concat(types.stream(), typeNames.stream()).map(t -> new Access(t, accessTypes));
                })
                .collect(Collectors.toList());
    }
}
