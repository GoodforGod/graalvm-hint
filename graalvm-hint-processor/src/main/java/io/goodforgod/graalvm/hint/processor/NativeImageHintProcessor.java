package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.*;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.processing.RoundEnvironment;
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
    protected Set<Class<? extends Annotation>> getSupportedAnnotations() {
        return Set.of(
                DynamicProxyHint.class,
                NativeImageHint.class,
                InitializationHint.class,
                InitializationHints.class);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotatedElements, RoundEnvironment roundEnv) {
        if (annotatedElements.isEmpty()) {
            return false;
        }

        try {
            final List<String> options = OPTION_PARSERS.stream()
                    .flatMap(parser -> parser.getOptions(roundEnv, processingEnv).stream())
                    .collect(Collectors.toList());

            if (options.isEmpty()) {
                final String annotations = OPTION_PARSERS.stream()
                        .flatMap(p -> p.annotations().stream()
                                .filter(a -> !roundEnv.getElementsAnnotatedWith(a).isEmpty()))
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(","));

                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                        annotations + " are present but no options retrieved");
                return false;
            } else {
                final String nativeImageProperties = options.stream()
                        .collect(Collectors.joining(ARG_SEPARATOR, "Args = ", ""));

                final HintOrigin origin = getHintOrigin(roundEnv, processingEnv);
                final String filePath = origin.getRelativePathForFile(FILE_NAME);
                return writeConfigFile(filePath, nativeImageProperties, processingEnv);
            }
        } catch (HintException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
