package io.graalvm.hint.processor;

import io.graalvm.hint.annotation.NativeImageHint;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/**
 * Please Add Description Here.
 *
 * @author Anton Kurako (GoodforGod)
 * @see NativeImageHint
 * @since 30.09.2021
 */
@SupportedAnnotationTypes("io.graalvm.hint.annotation.NativeImageHint")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedOptions({
        HintOptions.HINT_PROCESSING_GROUP,
        HintOptions.HINT_PROCESSING_ARTIFACT
})
public class NativeImageHintProcessor extends AbstractHintProcessor {

    private static final String FILE_NAME = "native-image.properties";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        final Set<? extends Element> annotated = roundEnv.getElementsAnnotatedWith(NativeImageHint.class);
        final Set<TypeElement> types = ElementFilter.typesIn(annotated);

        final String nativeImageProperties = getNativeImageProperties(types);
        if (nativeImageProperties.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING,
                    types.size() + " annotations @NativeImageHint are present but not Options or Entrypoint found!");
            return false;
        } else {
            return writeConfigFile(FILE_NAME, nativeImageProperties, roundEnv);
        }
    }

    private String getNativeImageProperties(Set<TypeElement> types) {
        final TypeElement element = types.iterator().next();

        final Optional<String> entryClassName = getAnnotationFieldClassNameAny(element, NativeImageHint.class.getSimpleName(), "entrypoint")
                .filter(v -> !v.equals(NativeImageHint.class.getSimpleName()));

        final String separator = " \\\n       ";
        final NativeImageHint imageHint = element.getAnnotation(NativeImageHint.class);
        final String options = Arrays.stream(imageHint.options())
                .map(String::strip)
                .collect(Collectors.joining(separator));

        return entryClassName
                .map(entry -> (options.isEmpty())
                        ? "Args = -H:Name=" + imageHint.name() + " -H:Class=" + entry
                        : "Args = -H:Name=" + imageHint.name() + " -H:Class=" + entry + separator + options)
                .orElseGet(() -> (options.isEmpty()) ? "" : "Args = " + options);
    }
}
