package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.ReflectionHint;
import io.goodforgod.graalvm.hint.annotation.ReflectionHint.AccessType;
import io.goodforgod.graalvm.hint.annotation.ReflectionHints;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.TypeElement;

/**
 * Processes {@link ReflectionHint} annotation for native-image reflect-config.json file
 *
 * @author Anton Kurako (GoodforGod)
 * @see ReflectionHint
 * @since 27.09.2021
 */
@SupportedAnnotationTypes({
        "io.goodforgod.graalvm.hint.annotation.ReflectionHint",
        "io.goodforgod.graalvm.hint.annotation.ReflectionHints"
})
@SupportedOptions({
        HintOrigin.HINT_PROCESSING_GROUP,
        HintOrigin.HINT_PROCESSING_ARTIFACT
})
public final class ReflectionHintProcessor extends AbstractAccessHintProcessor {

    @Override
    protected String getFileName() {
        return "reflect-config.json";
    }

    @Override
    protected String getEmptyConfigWarningMessage() {
        return "@ReflectionHint annotation found, but no reflection access hints parsed";
    }

    @Override
    protected Set<TypeElement> getAnnotatedTypeElements(RoundEnvironment roundEnv) {
        return getAnnotatedElements(roundEnv, ReflectionHint.class, ReflectionHints.class);
    }

    @Override
    protected Collection<Access> getGraalAccessForAnnotatedElement(TypeElement element) {
        final ReflectionHints hints = element.getAnnotation(ReflectionHints.class);
        if (hints == null) {
            final ReflectionHint reflectionHint = element.getAnnotation(ReflectionHint.class);
            return getGraalAccessForAnnotatedElement(element, reflectionHint, false);
        } else {
            return Arrays.stream(hints.value())
                    .flatMap(hint -> getGraalAccessForAnnotatedElement(element, hint, true).stream())
                    .collect(Collectors.toList());
        }
    }

    private Collection<Access> getGraalAccessForAnnotatedElement(TypeElement element,
                                                                 ReflectionHint hint,
                                                                 boolean isParentAnnotation) {
        final AccessType[] accessTypes = hint.value();
        final List<String> typeNames = Arrays.asList(hint.typeNames());
        final List<String> types = (!isParentAnnotation)
                ? getAnnotationFieldClassNames(element, ReflectionHint.class, "types")
                : getAnnotationFieldClassNames(element, ReflectionHint.class, "types", ReflectionHints.class,
                        getParentAnnotationPredicate(accessTypes));

        if (types.isEmpty() && typeNames.isEmpty()) {
            final String selfName = element.getQualifiedName().toString();
            return List.of(new Access(selfName, accessTypes));
        }

        return Stream.concat(types.stream(), typeNames.stream())
                .map(t -> new Access(t, accessTypes))
                .collect(Collectors.toList());
    }
}
