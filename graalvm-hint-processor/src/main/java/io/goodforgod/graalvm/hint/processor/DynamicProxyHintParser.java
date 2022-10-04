package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.DynamicProxyHint;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
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
    public List<Class<? extends Annotation>> getSupportedAnnotations() {
        return List.of(DynamicProxyHint.class);
    }

    @Override
    public List<String> getOptions(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
        final Set<? extends Element> annotated = roundEnv.getElementsAnnotatedWith(DynamicProxyHint.class);
        final Set<TypeElement> elements = ElementFilter.typesIn(annotated);
        if (elements.isEmpty()) {
            return Collections.emptyList();
        }

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

            final HintOrigin origin = HintUtils.getHintOrigin(roundEnv, processingEnv);
            final HintFile file = origin.getFileWithRelativePath("dynamic-proxy-config.json");
            HintUtils.writeConfigFile(file, proxyConfigurationFile, processingEnv);
            resources.add(file.getPath());
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

    private List<Configuration> getDynamicProxyConfigurations(TypeElement type) {
        final String annotationParent = DynamicProxyHint.class.getSimpleName();
        final List<Configuration> interfaceConfigurations = type.getAnnotationMirrors().stream()
                .filter(pa -> pa.getAnnotationType().asElement().getSimpleName().contentEquals(annotationParent))
                .flatMap(pa -> pa.getElementValues().entrySet().stream())
                .filter(e -> e.getKey().getSimpleName().contentEquals("value"))
                .flatMap(e -> ((List<?>) e.getValue().getValue()).stream())
                .map(a -> {
                    final List<String> interfaces = HintUtils.getAnnotationFieldValues((AnnotationMirror) a, "interfaces");
                    return new Configuration(interfaces);
                })
                .collect(Collectors.toList());

        if (interfaceConfigurations.isEmpty() && isSelfConfiguration(type)) {
            final String elementName = type.getQualifiedName().toString();
            if (type.getKind().isInterface()) {
                return List.of(new Configuration(List.of(elementName)));
            } else {
                throw new HintException(elementName + " is annotated with @"
                        + DynamicProxyHint.class.getSimpleName() + " hint but is not an interface");
            }
        }

        return interfaceConfigurations;
    }

    private boolean isSelfConfiguration(TypeElement element) {
        final DynamicProxyHint annotation = element.getAnnotation(DynamicProxyHint.class);
        return (annotation.resources() == null || annotation.resources().length == 0)
                && (annotation.files() == null || annotation.files().length == 0);
    }
}
