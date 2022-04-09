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
        return Set.of(
                ReflectionHint.class,
                ReflectionHints.class);
    }

    @Override
    protected String getFileName() {
        return "reflect-config.json";
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
                ? HintUtils.getAnnotationFieldClassNames(element, ReflectionHint.class, "types")
                : HintUtils.getAnnotationFieldClassNames(element, ReflectionHint.class, "types", ReflectionHints.class,
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
