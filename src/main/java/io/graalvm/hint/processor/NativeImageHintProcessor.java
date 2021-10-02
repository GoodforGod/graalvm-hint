package io.graalvm.hint.processor;

import io.graalvm.hint.annotation.NativeImageHint;
import java.util.Arrays;
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
        if (types.size() > 1) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Only one @NativeImageHint can be present");
            return false;
        }

        final String nativeImageProperties = getNativeImageProperties(types);
        return writeConfigFile(FILE_NAME, nativeImageProperties, roundEnv);
    }

    private String getNativeImageProperties(Set<TypeElement> types) {
        final TypeElement element = types.iterator().next();

        final String entryClassName = getAnnotationFieldClassNameAny(element, NativeImageHint.class.getSimpleName(), "entrypoint")
                .filter(v -> !v.equals(NativeImageHint.class.getSimpleName())) // check that not default value
                .orElseGet(() -> element.getQualifiedName().toString());

        final String separator = " \\\n       ";
        final NativeImageHint imageHint = element.getAnnotation(NativeImageHint.class);
        final String options = Arrays.stream(imageHint.options())
                .map(String::strip)
                .collect(Collectors.joining(separator));

        final String mainArguments = "Args = -H:Name=" + imageHint.name() + " -H:+InlineBeforeAnalysis -H:Class=" + entryClassName;
        return options.isEmpty()
                ? mainArguments
                : mainArguments + separator + options;
    }
}
