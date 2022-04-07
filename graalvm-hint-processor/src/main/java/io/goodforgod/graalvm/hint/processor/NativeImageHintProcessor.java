package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Processes {@link NativeImageHint} and {@link InitializationHint} and {@link DynamicProxyHint}
 * annotations for
 * native-image.properties file
 *
 * @author Anton Kurako (GoodforGod)
 * @see DynamicProxyHint
 * @see NativeImageHint
 * @see InitializationHint
 * @since 30.09.2021
 */
@SupportedAnnotationTypes({
        "io.goodforgod.graalvm.hint.annotation.DynamicProxyHint",
        "io.goodforgod.graalvm.hint.annotation.NativeImageHint",
        "io.goodforgod.graalvm.hint.annotation.InitializationHint",
        "io.goodforgod.graalvm.hint.annotation.InitializationHints"
})
@SupportedOptions({
        HintOrigin.HINT_PROCESSING_GROUP,
        HintOrigin.HINT_PROCESSING_ARTIFACT
})
public final class NativeImageHintProcessor extends AbstractHintProcessor {

    private static final String FILE_NAME = "native-image.properties";
    private static final String ARG_SEPARATOR = " \\\n       ";

    private static final NativeImageHintParser NATIVE_IMAGE_HINT_PARSER = new NativeImageHintParser();
    private static final InitializationHintParser INITIALIZATION_HINT_PARSER = new InitializationHintParser();
    private static final DynamicProxyHintParser DYNAMIC_PROXY_HINT_PARSER = new DynamicProxyHintParser();

    private static final List<OptionParser> OPTION_PARSERS = List.of(
            NATIVE_IMAGE_HINT_PARSER,
            INITIALIZATION_HINT_PARSER,
            DYNAMIC_PROXY_HINT_PARSER);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        try {
            final List<String> options = OPTION_PARSERS.stream()
                    .flatMap(parser -> parser.getOptions(roundEnv, processingEnv).stream())
                    .collect(Collectors.toList());

            if (options.isEmpty()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING,
                        "@NativeImageHint are present but no options found");
                return false;
            } else {
                final String nativeImageProperties = options.stream()
                        .collect(Collectors.joining(ARG_SEPARATOR, "Args = ", ""));

                final HintOrigin origin = getHintOrigin(roundEnv, processingEnv);
                final String filePath = origin.getRelativePathForFile(FILE_NAME);
                return writeConfigFile(filePath, nativeImageProperties, processingEnv);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
