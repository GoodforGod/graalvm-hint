package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.JniHint;
import io.goodforgod.graalvm.hint.annotation.JniHints;
import io.goodforgod.graalvm.hint.annotation.ReflectionHint;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.TypeElement;

/**
 * Processes {@link JniHint} annotation for native-image reflect-config.json file
 *
 * @see JniHint
 * @author Anton Kurako (GoodforGod)
 * @since 21.03.2022
 */
@SupportedAnnotationTypes({
        "io.goodforgod.graalvm.hint.annotation.JniHint",
        "io.goodforgod.graalvm.hint.annotation.JniHints"
})
@SupportedOptions({
        HintOptions.HINT_PROCESSING_GROUP,
        HintOptions.HINT_PROCESSING_ARTIFACT
})
public final class JniHintProcessor extends AbstractAccessHintProcessor {

    private static final Map<String, ReflectionHint.AccessType> ACCESS_TYPE_MAP = Arrays
            .stream(ReflectionHint.AccessType.values())
            .collect(Collectors.toMap(Enum::name, e -> e));

    @Override
    protected String getFileName() {
        return "jni-config.json";
    }

    @Override
    protected String getEmptyConfigWarningMessage() {
        return "@JniHint annotation found, but no reflection access hints parsed";
    }

    @Override
    protected Set<TypeElement> getAnnotatedTypeElements(RoundEnvironment roundEnv) {
        return getAnnotatedElements(roundEnv, JniHint.class, JniHints.class);
    }

    @Override
    protected Collection<Access> getGraalAccessForAnnotatedElement(TypeElement element) {
        final JniHints hints = element.getAnnotation(JniHints.class);
        if (hints == null) {
            final JniHint reflectionHint = element.getAnnotation(JniHint.class);
            return getGraalReflectionsForAnnotatedElement(element, reflectionHint, false);
        } else {
            return Arrays.stream(hints.value())
                    .flatMap(hint -> getGraalReflectionsForAnnotatedElement(element, hint, true).stream())
                    .collect(Collectors.toList());
        }
    }

    private Collection<Access> getGraalReflectionsForAnnotatedElement(TypeElement element,
                                                                      JniHint hint,
                                                                      boolean isParentAnnotation) {
        final ReflectionHint.AccessType[] accessTypes = convert(hint.value());
        final List<String> typeNames = Arrays.asList(hint.typeNames());
        final List<String> types = (!isParentAnnotation)
                ? getAnnotationFieldClassNames(element, JniHint.class, "types")
                : getAnnotationFieldClassNames(element, JniHint.class, "types", JniHints.class,
                        getParentAnnotationPredicate(accessTypes));

        if (types.isEmpty() && typeNames.isEmpty()) {
            final String selfName = element.getQualifiedName().toString();
            return List.of(new Access(selfName, accessTypes));
        }

        return Stream.concat(types.stream(), typeNames.stream())
                .map(type -> new Access(type, accessTypes))
                .collect(Collectors.toList());
    }

    private static ReflectionHint.AccessType[] convert(JniHint.AccessType[] accessTypes) {
        return Arrays.stream(accessTypes)
                .map(accessType -> ACCESS_TYPE_MAP.get(accessType.name()))
                .toArray(ReflectionHint.AccessType[]::new);
    }
}
