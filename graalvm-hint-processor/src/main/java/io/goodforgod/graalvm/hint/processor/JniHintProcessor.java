package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.JniHint;
import io.goodforgod.graalvm.hint.annotation.JniHints;
import io.goodforgod.graalvm.hint.annotation.ReflectionHint;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

/**
 * Processes {@link JniHint} annotation for native-image reflect-config.json file
 *
 * @see JniHint
 * @author Anton Kurako (GoodforGod)
 * @since 21.03.2022
 */
public final class JniHintProcessor extends AbstractAccessHintProcessor {

    private static final Map<String, ReflectionHint.AccessType> ACCESS_TYPE_MAP = Arrays
            .stream(ReflectionHint.AccessType.values())
            .collect(Collectors.toMap(Enum::name, e -> e));

    @Override
    protected Set<Class<? extends Annotation>> getSupportedAnnotations() {
        return Set.of(JniHint.class, JniHints.class);
    }

    @Override
    protected String getFileName() {
        return "jni-config.json";
    }

    @Override
    protected Collection<Access> getGraalAccessForAnnotatedElement(TypeElement element) {
        final JniHints hints = element.getAnnotation(JniHints.class);
        if (hints == null) {
            final JniHint hint = element.getAnnotation(JniHint.class);
            return getAnnotationAccesses(element, hint);
        } else {
            return getParentAnnotationAccesses(element, JniHint.class, JniHints.class);
        }
    }

    private static Collection<Access> getAnnotationAccesses(TypeElement element,
                                                            JniHint hint) {
        final ReflectionHint.AccessType[] accessTypes = convert(hint.value());
        final List<String> typeNames = Arrays.asList(hint.typeNames());
        final List<String> types = HintUtils.getAnnotationFieldClassNames(element, JniHint.class, "types");
        if (types.isEmpty() && typeNames.isEmpty()) {
            final String selfName = element.getQualifiedName().toString();
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
                    final ReflectionHint.AccessType[] accessTypes = convert(HintUtils
                            .getAnnotationFieldValuesOrDefault(a, "value", List.of(JniHint.AccessType.ALL_DECLARED.name()))
                            .stream()
                            .map(JniHint.AccessType::valueOf)
                            .toArray(JniHint.AccessType[]::new));

                    return (types.isEmpty() && typeNames.isEmpty())
                            ? Stream.of(new Access(type.getQualifiedName().toString(), accessTypes))
                            : Stream.concat(types.stream(), typeNames.stream()).map(t -> new Access(t, accessTypes));
                })
                .collect(Collectors.toList());
    }

    private static ReflectionHint.AccessType[] convert(JniHint.AccessType[] accessTypes) {
        return Arrays.stream(accessTypes)
                .map(accessType -> ACCESS_TYPE_MAP.get(accessType.name()))
                .toArray(ReflectionHint.AccessType[]::new);
    }
}
