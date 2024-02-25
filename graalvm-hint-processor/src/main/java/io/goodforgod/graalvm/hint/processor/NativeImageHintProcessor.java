package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.DynamicProxyHint;
import io.goodforgod.graalvm.hint.annotation.InitializationHint;
import io.goodforgod.graalvm.hint.annotation.LinkHint;
import io.goodforgod.graalvm.hint.annotation.NativeImageHint;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Processes {@link OptionParser} annotations for native-image.properties file.
 *
 * @author Anton Kurako (GoodforGod)
 * @see DynamicProxyHint
 * @see NativeImageHint
 * @see InitializationHint
 * @see LinkHint
 * @since 30.09.2021
 */
public final class NativeImageHintProcessor extends AbstractHintProcessor {

    private static final String FILE_NAME = "native-image.properties";
    private static final String ARG_SEPARATOR = " \\\n       ";

    private static final List<OptionParser> OPTION_PARSERS = List.of(
            new NativeImageHintParser(),
            new InitializationHintParser(),
            new LinkHintParser(),
            new DynamicProxyHintParser());

    @Override
    protected Set<Class<? extends Annotation>> getSupportedAnnotations() {
        return OPTION_PARSERS.stream()
                .flatMap(p -> p.getSupportedAnnotations().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotatedElements, RoundEnvironment roundEnv) {
        if (annotatedElements.isEmpty()) {
            return false;
        }

        try {
            final List<Option> options = OPTION_PARSERS.stream()
                    .flatMap(parser -> parser.getOptions(roundEnv, processingEnv).stream())
                    .collect(Collectors.groupingBy(Option::getOrigin))
                    .entrySet().stream()
                    .map(e -> {
                        final List<String> mergedOptions = e.getValue().stream()
                                .flatMap(v -> v.getOptions().stream())
                                .distinct()
                                .collect(Collectors.toList());
                        return new Option(e.getKey(), mergedOptions);
                    })
                    .collect(Collectors.toList());

            if (options.isEmpty()) {
                final String annotations = OPTION_PARSERS.stream()
                        .flatMap(parser -> parser.getSupportedAnnotations().stream()
                                .filter(a -> !roundEnv.getElementsAnnotatedWith(a).isEmpty()))
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(","));

                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        annotations + " are present but no options retrieved");
                return false;
            } else {
                for (Option option : options) {
                    final String nativeImageProperties = option.getOptions().stream()
                            .collect(Collectors.joining(ARG_SEPARATOR, "Args = ", ""));

                    final HintFile file = option.getOrigin().getFileWithRelativePath(FILE_NAME);
                    if (!HintUtils.writeConfigFile(file, nativeImageProperties, processingEnv)) {
                        return false;
                    }
                }

                return true;
            }
        } catch (HintException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), e.getElement());
            return false;
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
