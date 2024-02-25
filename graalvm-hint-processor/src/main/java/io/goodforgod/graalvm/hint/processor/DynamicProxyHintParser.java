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

    private static final class DynamicProxy {

        private final List<String> resources = new ArrayList<>();
        private final List<String> files = new ArrayList<>();
        private final List<Configuration> configurations = new ArrayList<>();
    }

    @Override
    public List<Class<? extends Annotation>> getSupportedAnnotations() {
        return List.of(DynamicProxyHint.class);
    }

    @Override
    public List<Option> getOptions(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
        final Set<? extends Element> annotated = roundEnv.getElementsAnnotatedWith(DynamicProxyHint.class);
        final Set<TypeElement> elements = ElementFilter.typesIn(annotated);
        if (elements.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<HintOrigin, DynamicProxy> proxies = new HashMap<>();
        for (TypeElement element : elements) {
            final List<String> resources = List.of(element.getAnnotation(DynamicProxyHint.class).resources());
            final List<String> files = List.of(element.getAnnotation(DynamicProxyHint.class).files());
            final List<Configuration> configurations = getDynamicProxyConfigurations(element).stream()
                    .filter(c -> !c.getInterfaces().isEmpty())
                    .collect(Collectors.toList());

            if (!configurations.isEmpty() || !files.isEmpty() || !resources.isEmpty()) {
                final HintOrigin origin = HintUtils.getHintOrigin(element, processingEnv);
                final DynamicProxy proxy = proxies.computeIfAbsent(origin, k -> new DynamicProxy());
                proxy.files.addAll(files);
                proxy.resources.addAll(resources);
                proxy.configurations.addAll(configurations);
            }
        }

        proxies.forEach((o, p) -> {
            if (!p.configurations.isEmpty()) {
                final String proxyConfigurationFile = p.configurations.stream()
                        .map(c -> c.getInterfaces().stream()
                                .collect(Collectors.joining("\", \"", "  { \"interfaces\": [ \"", "\" ] }")))
                        .collect(Collectors.joining(",\n", "[\n", "\n]"));

                final HintOrigin origin = HintUtils.getHintOrigin(elements.iterator().next(), processingEnv);
                final HintFile file = origin.getFileWithRelativePath("dynamic-proxy-config.json");
                HintUtils.writeConfigFile(file, proxyConfigurationFile, processingEnv);
                p.resources.add(file.getPath());
            }
        });

        final List<Option> options = new ArrayList<>();
        proxies.forEach((o, p) -> {
            final List<String> originOptions = new ArrayList<>();
            if (!p.files.isEmpty()) {
                final String proxyFileOption = p.files.stream()
                        .collect(Collectors.joining(",", "-H:DynamicProxyConfigurationFiles=", ""));
                originOptions.add(proxyFileOption);
            }

            if (!p.resources.isEmpty()) {
                final String proxyResourceOption = p.resources.stream()
                        .collect(Collectors.joining(",", "-H:DynamicProxyConfigurationResources=", ""));
                originOptions.add(proxyResourceOption);
            }

            options.add(new Option(o, originOptions));
        });

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
            final String elementName = HintUtils.getElementClassName(type);
            if (type.getKind().isInterface()) {
                return List.of(new Configuration(List.of(elementName)));
            } else {
                throw new HintException(elementName + " is annotated with @"
                        + DynamicProxyHint.class.getSimpleName() + " hint but is not an interface", type);
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
