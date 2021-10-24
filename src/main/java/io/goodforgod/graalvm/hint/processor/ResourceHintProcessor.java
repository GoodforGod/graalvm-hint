package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.ResourceHint;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/**
 * Processes {@link ResourceHint} annotation for native-image resource-config.json file
 *
 * @author Anton Kurako (GoodforGod)
 * @see ResourceHint
 * @since 27.09.2021
 */
@SupportedAnnotationTypes("io.goodforgod.graalvm.hint.annotation.ResourceHint")
@SupportedOptions({
        HintOptions.HINT_PROCESSING_GROUP,
        HintOptions.HINT_PROCESSING_ARTIFACT
})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class ResourceHintProcessor extends AbstractHintProcessor {

    private static final String FILE_NAME = "resource-config.json";
    private static final String PATTERN = "pattern";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        try {
            final Set<? extends Element> annotated = roundEnv.getElementsAnnotatedWith(ResourceHint.class);
            final Set<TypeElement> types = ElementFilter.typesIn(annotated);

            final Optional<String> resourceConfigJson = getResourceConfigJsonValue(types);
            if (resourceConfigJson.isEmpty()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING,
                        "@ResourceHint found, but patterns are not present");
                return false;
            }

            return writeConfigFile(FILE_NAME, resourceConfigJson.get(), roundEnv);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Optional<String> getResourceConfigJsonValue(Set<TypeElement> types) {
        final Collection<String> resourcePatterns = getGraalVMResourcePattens(types);
        if (resourcePatterns.isEmpty()) {
            return Optional.empty();
        }

        final String resourceConfig = resourcePatterns.stream()
                .map(ResourceHintProcessor::mapPatternToGraalVM)
                .sorted()
                .map(resource -> Map.of(PATTERN, resource))
                .flatMap(m -> m.entrySet().stream())
                .map(r -> String.format("    { \"%s\" : \"%s\" }", r.getKey(), r.getValue()))
                .collect(Collectors.joining(",\n", "{\n  \"resources\": [\n", "\n  ]\n}"));

        return Optional.of(resourceConfig);
    }

    private static Collection<String> getGraalVMResourcePattens(Set<TypeElement> types) {
        return types.stream()
                .flatMap(t -> Arrays.stream(t.getAnnotation(ResourceHint.class).patterns()))
                .filter(r -> !r.isBlank())
                .collect(Collectors.toSet());
    }

    private static String mapPatternToGraalVM(String pattern) {
        return pattern;
    }
}
