package io.goodforgod.graalvm.hint.processor;

import static io.goodforgod.graalvm.hint.processor.AbstractHintProcessor.getHintOrigin;

import io.goodforgod.graalvm.hint.annotation.DynamicProxyHint;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

/**
 * Processes {@link DynamicProxyHint} annotations for native-image.properties file
 *
 * @author Anton Kurako (GoodforGod)
 * @see DynamicProxyHint
 * @author Anton Kurako (GoodforGod)
 * @since 07.04.2022
 */
final class DynamicProxyHintParser implements OptionParser {

    static class Configuration {

        private final List<String> interfaces;

        private Configuration(List<String> interfaces) {
            this.interfaces = interfaces;
        }

        public List<String> getInterfaces() {
            return interfaces;
        }
    }

    @Override
    public List<String> getOptions(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
        final Set<? extends Element> annotated = roundEnv.getElementsAnnotatedWith(DynamicProxyHint.class);
        final Set<TypeElement> elements = ElementFilter.typesIn(annotated);

        final List<String> resources = elements.stream()
                .map(element -> element.getAnnotation(DynamicProxyHint.class))
                .flatMap(hint -> Arrays.stream(hint.resources()))
                .collect(Collectors.toList());

        final List<String> files = elements.stream()
                .map(element -> element.getAnnotation(DynamicProxyHint.class))
                .flatMap(hint -> Arrays.stream(hint.files()))
                .collect(Collectors.toList());

        final List<Configuration> configurations = elements.stream()
                .map(this::getDynamicProxyConfigurations)
                .flatMap(Collection::stream)
                .filter(c -> !c.getInterfaces().isEmpty())
                .collect(Collectors.toList());

        if (!configurations.isEmpty()) {
            final String proxyConfigurationFile = configurations.stream()
                    .map(c -> c.getInterfaces().stream()
                            .collect(Collectors.joining("\", \"", "  { \"interfaces\": [ \"", "\" ] }")))
                    .collect(Collectors.joining(",\n", "[\n", "\n]"));

            final HintOrigin origin = getHintOrigin(roundEnv, processingEnv);
            final String filePath = origin.getRelativePathForFile("dynamic-proxy-hint.json");
            AbstractHintProcessor.writeConfigFile(filePath, proxyConfigurationFile, processingEnv);
            resources.add(filePath);
        }

        final List<String> options = new ArrayList<>();
        if (!files.isEmpty()) {
            final String proxyFileOption = files.stream()
                    .collect(Collectors.joining(",", "-H:DynamicProxyConfigurationFiles=", ""));
            options.add(proxyFileOption);
        }

        if (!resources.isEmpty()) {
            final String proxyResourceOption = resources.stream()
                    .collect(Collectors.joining(",", "-H:DynamicProxyConfigurationResources=", ""));
            options.add(proxyResourceOption);
        }

        return options;
    }

    private List<Configuration> getDynamicProxyConfigurations(TypeElement element) {
        final String annotationName = DynamicProxyHint.Configuration.class.getSimpleName();
        final String annotationParent = DynamicProxyHint.class.getSimpleName();
        final AnnotationTypeFieldVisitor visitor = new AnnotationTypeFieldVisitor(annotationName, "interfaces");
        final List<Configuration> configurations = element.getAnnotationMirrors().stream()
                .filter(a -> a.getAnnotationType().asElement().getSimpleName().contentEquals(annotationParent))
                .flatMap(a -> a.getElementValues().entrySet().stream()
                        .filter(e -> e.getKey().getSimpleName().contentEquals("value"))
                        .flatMap(e -> ((List<?>) e.getValue().getValue()).stream()
                                .map(an -> {
                                    final List<String> interfaces = ((AnnotationValue) an).accept(visitor, "").stream()
                                            .map(c -> c.substring(0, c.length() - 6))
                                            .collect(Collectors.toList());

                                    return new Configuration(interfaces);
                                })))
                .collect(Collectors.toList());

        return configurations;
    }
}
