package io.graalvm.hint.processor;

import io.graalvm.hint.annotation.ResourceHint;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Please Add Description Here.
 *
 * @author Anton Kurako (GoodforGod)
 * @see ResourceHint
 * @since 27.09.2021
 */
@SupportedAnnotationTypes("io.graalvm.hint.annotation.ResourceHint")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedOptions({
        HintOptions.HINT_PROCESSING_GROUP,
        HintOptions.HINT_PROCESSING_ARTIFACT
})
public class ResourceHintProcessor extends AbstractHintProcessor {

    private static final String FILE_NAME = "resource-config.json";
    private static final String PATTERN = "pattern";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        final Set<? extends Element> annotated = roundEnv.getElementsAnnotatedWith(ResourceHint.class);
        final Set<TypeElement> types = ElementFilter.typesIn(annotated);

        final Optional<String> resourceConfigJson = getResourceConfigJsonValue(types);
        if (resourceConfigJson.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING,
                    "@ResourceHint found, but patterns are not present");
            return false;
        }

        final HintOptions hintOptions = getHintOptions(roundEnv);
        final String path = hintOptions.getRelativePathForFile(FILE_NAME);
        try {
            final FileObject fileObject = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", path);
            try (Writer writer = fileObject.openWriter()) {
                writer.write(resourceConfigJson.get());
            }
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Couldn't write " + FILE_NAME + " due to: " + e.getMessage());
            return false;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generated " + FILE_NAME + " file to: " + path);
        return true;
    }

    private static Optional<String> getResourceConfigJsonValue(Set<TypeElement> types) {
        final List<String> resources = types.stream()
                .flatMap(t -> Arrays.stream(t.getAnnotation(ResourceHint.class).patterns()))
                .filter(r -> !r.isBlank() && !r.equals("."))
                .collect(Collectors.toList());

        if (resources.isEmpty())
            return Optional.empty();

        return Optional.of(resources.stream()
                .map(ResourceHintProcessor::mapToResource)
                .flatMap(r -> r.entrySet().stream().map(e -> "    { \"" + e.getKey() + "\" : \"" + e.getValue() + "\" }"))
                .collect(Collectors.joining(",\n", "{\n  \"resources\": [\n", "\n  ]\n}")));
    }

    private static Map<String, String> mapToResource(String resourcePattern) {
        return (resourcePattern.contains("*"))
                ? Map.of(PATTERN, resourcePattern)
                : Map.of(PATTERN, "\\\\Q" + resourcePattern + "\\\\E");
    }
}
