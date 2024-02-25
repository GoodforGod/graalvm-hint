package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.LinkHint;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Processes {@link LinkHint} annotations for native-image.properties file
 *
 * @author Anton Kurako (GoodforGod)
 * @author Anton Kurako (GoodforGod)
 * @see LinkHint
 * @since 02.05.2022
 */
final class LinkHintParser implements OptionParser {

    private static final String LINK_BUILD_TIME = "--link-at-build-time";

    @Override
    public List<Class<? extends Annotation>> getSupportedAnnotations() {
        return List.of(LinkHint.class);
    }

    @Override
    public List<Option> getOptions(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
        final Set<TypeElement> elements = HintUtils.getAnnotatedElements(roundEnv, LinkHint.class);
        if (elements.isEmpty()) {
            return Collections.emptyList();
        }

        final Optional<TypeElement> linkAll = elements.stream()
                .filter(e -> e.getAnnotation(LinkHint.class).all())
                .findFirst();
        if (linkAll.isPresent()) {
            final HintOrigin hintOrigin = HintUtils.getHintOrigin(linkAll.get(), processingEnv);
            return List.of(new Option(hintOrigin, List.of(LINK_BUILD_TIME)));
        }

        final Map<HintOrigin, List<String>> links = new HashMap<>();
        for (TypeElement element : elements) {
            final LinkHint hint = element.getAnnotation(LinkHint.class);
            final List<String> hintTypes = getTypes(element, hint).collect(Collectors.toList());

            if (!hintTypes.isEmpty()) {
                final HintOrigin origin = HintUtils.getHintOrigin(element, processingEnv);
                final List<String> options = links.computeIfAbsent(origin, k -> new ArrayList<>());
                options.addAll(hintTypes);
            }
        }

        return links.entrySet().stream()
                .map(e -> {
                    final String linkOption = e.getValue().stream()
                            .distinct()
                            .collect(Collectors.joining(",", LINK_BUILD_TIME + "=", ""));

                    return new Option(e.getKey(), List.of(linkOption));
                })
                .collect(Collectors.toList());
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
