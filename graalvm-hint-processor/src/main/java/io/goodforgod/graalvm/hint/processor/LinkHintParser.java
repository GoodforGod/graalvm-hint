package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.LinkHint;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Processes {@link LinkHint} annotations for native-image.properties file
 *
 * @author Anton Kurako (GoodforGod)
 * @see LinkHint
 * @author Anton Kurako (GoodforGod)
 * @since 02.05.2022
 */
final class LinkHintParser implements OptionParser {

    private static final String LINK_BUILD_TIME = "--link-at-build-time";

    @Override
    public List<Class<? extends Annotation>> getSupportedAnnotations() {
        return List.of(LinkHint.class);
    }

    @Override
    public List<String> getOptions(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
        final Set<TypeElement> elements = HintUtils.getAnnotatedElements(roundEnv, LinkHint.class);
        if (elements.isEmpty()) {
            return Collections.emptyList();
        }

        final boolean linkAll = elements.stream().anyMatch(e -> e.getAnnotation(LinkHint.class).all());
        if (linkAll) {
            return List.of(LINK_BUILD_TIME);
        }

        final List<String> types = elements.stream()
                .flatMap(e -> {
                    final LinkHint hint = e.getAnnotation(LinkHint.class);
                    return getTypes(e, hint);
                })
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return List.of(types.stream()
                .collect(Collectors.joining(",", LINK_BUILD_TIME + "=", "")));
    }

    private Stream<String> getTypes(TypeElement element, LinkHint hint) {
        final List<String> types = HintUtils.getAnnotationFieldClassNames(element, LinkHint.class, "types");
        final List<String> typeNames = Arrays.asList(hint.typeNames());
        if (types.isEmpty() && typeNames.isEmpty()) {
            return Stream.of(HintUtils.getElementClassName(element));
        } else {
            final Stream<String> typeStream = types.stream().map(c -> c.endsWith(".class")
                    ? c.substring(0, c.length() - 6)
                    : c);

            return Stream.concat(typeStream, typeNames.stream());
        }
    }
}
