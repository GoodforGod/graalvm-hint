package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.ResourceHint;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.processing.RoundEnvironment;
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
public final class ResourceHintProcessor extends AbstractHintProcessor {

    private static final String FILE_NAME = "resource-config.json";

    private static class Resources {

        private final Set<String> includes = new HashSet<>();
        private final Set<String> excludes = new HashSet<>();
        private final Set<String> bundles = new HashSet<>();

        boolean haveIncludes() {
            return !includes.isEmpty();
        }

        boolean haveExcludes() {
            return !excludes.isEmpty();
        }

        boolean haveBundles() {
            return !bundles.isEmpty();
        }
    }

    @Override
    protected Set<Class<? extends Annotation>> getSupportedAnnotations() {
        return Set.of(ResourceHint.class);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        try {
            final Set<? extends Element> annotated = roundEnv.getElementsAnnotatedWith(ResourceHint.class);
            final Set<TypeElement> elements = ElementFilter.typesIn(annotated);

            final String resourceConfigJson = getResourceConfig(elements);

            final HintOrigin origin = HintUtils.getHintOrigin(roundEnv, processingEnv);
            final String filePath = origin.getRelativePathForFile(FILE_NAME);
            return HintUtils.writeConfigFile(filePath, resourceConfigJson, processingEnv);
        } catch (HintException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getResourceConfig(Set<TypeElement> elements) {
        final Resources resources = getResources(elements);
        final StringBuilder configBuilder = new StringBuilder();
        configBuilder.append("{");
        if (resources.haveIncludes() || resources.haveExcludes()) {
            configBuilder.append("\n  \"resources\": {\n");
            if (resources.haveIncludes()) {
                final String includePart = getResourceConfigPart("includes", resources.includes);
                configBuilder.append(includePart);
            }

            if (resources.haveExcludes()) {
                if (resources.haveIncludes()) {
                    configBuilder.append(",\n");
                }

                final String excludePart = getResourceConfigPart("excludes", resources.excludes);
                configBuilder.append(excludePart);
            }

            if (resources.haveBundles()) {
                configBuilder.append("\n  },");
            } else {
                configBuilder.append("\n  }");
            }
        }

        if (resources.haveBundles()) {
            configBuilder.append("\n");
            final String bundlePart = getBundleConfigPart(resources.bundles);
            configBuilder.append(bundlePart);
        }

        configBuilder.append("\n}");

        return configBuilder.toString();
    }

    private static String getResourceConfigPart(String partName, Collection<String> resources) {
        return resources.stream()
                .sorted()
                .map(resource -> String.format("      { \"%s\": \"%s\" }", "pattern", resource))
                .collect(Collectors.joining(",\n", "    \"" + partName + "\": [\n", "\n    ]"));
    }

    private static String getBundleConfigPart(Collection<String> bundles) {
        return bundles.stream()
                .sorted()
                .map(bundle -> String.format("    { \"%s\": \"%s\" }", "name", bundle))
                .collect(Collectors.joining(",\n", "  \"bundles\": [\n", "\n  ]"));
    }

    private static Resources getResources(Set<TypeElement> elements) {
        final Resources resources = new Resources();
        for (TypeElement element : elements) {
            final ResourceHint annotation = element.getAnnotation(ResourceHint.class);
            final List<String> includeBatch = filterValues(annotation.include());
            final List<String> excludeBatch = filterValues(annotation.exclude());
            final List<String> bundleBatch = filterValues(annotation.bundles());
            if (includeBatch.isEmpty() && excludeBatch.isEmpty() && bundleBatch.isEmpty()) {
                throw new HintException(element.getQualifiedName().toString() + " is annotated with @"
                        + ResourceHint.class.getSimpleName()
                        + ", but no valid 'include' or 'exclude' or 'bundle' parameters specified!");
            }

            resources.includes.addAll(includeBatch);
            resources.excludes.addAll(excludeBatch);
            resources.bundles.addAll(bundleBatch);
        }

        return resources;
    }

    private static List<String> filterValues(String[] stringValues) {
        return Arrays.stream(stringValues)
                .filter(a -> !a.isBlank())
                .collect(Collectors.toList());
    }
}
