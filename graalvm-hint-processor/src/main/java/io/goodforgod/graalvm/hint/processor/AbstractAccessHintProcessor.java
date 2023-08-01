package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.JniHint;
import io.goodforgod.graalvm.hint.annotation.ReflectionHint;
import io.goodforgod.graalvm.hint.annotation.ReflectionHint.AccessType;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Processes {@link ReflectionHint} and {@link JniHint} annotations for native-image hint files
 *
 * @author Anton Kurako (GoodforGod)
 * @see ReflectionHint
 * @see JniHint
 * @since 21.03.2022
 */
abstract class AbstractAccessHintProcessor extends AbstractHintProcessor {

    private static final String ALL_PUBLIC_CONSTRUCTORS = "allPublicConstructors";
    private static final String ALL_PUBLIC_FIELDS = "allPublicFields";
    private static final String ALL_PUBLIC_METHODS = "allPublicMethods";

    private static final String ALL_DECLARED_CONSTRUCTORS = "allDeclaredConstructors";
    private static final String ALL_DECLARED_FIELDS = "allDeclaredFields";
    private static final String ALL_DECLARED_METHODS = "allDeclaredMethods";

    private static final String NAME = "name";

    static class Access implements Comparable<Access> {

        private final String typeName;
        private final AccessType[] accessTypes;

        Access(String typeName, AccessType[] accessTypes) {
            this.typeName = typeName;
            this.accessTypes = accessTypes;
        }

        @Override
        public int compareTo(Access o) {
            return typeName.compareTo(o.typeName);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Access that = (Access) o;
            return Objects.equals(typeName, that.typeName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(typeName);
        }
    }

    protected abstract String getFileName();

    protected abstract Collection<Access> getGraalAccessForAnnotatedElement(TypeElement element);

    protected Set<TypeElement> getAnnotatedTypeElements(RoundEnvironment roundEnv) {
        final Class[] classes = getSupportedAnnotations().toArray(Class[]::new);
        return HintUtils.getAnnotatedElements(roundEnv, classes);
    }

    protected Predicate<AnnotationValue> getParentAnnotationPredicate(ReflectionHint.AccessType[] accessTypes) {
        return a -> {
            final List<String> accessTypeNames = Arrays.stream(accessTypes)
                    .map(Enum::name)
                    .collect(Collectors.toList());

            return ((AnnotationMirror) a).getElementValues().entrySet().stream()
                    .filter(e -> e.getKey().getSimpleName().contentEquals("value"))
                    .anyMatch(entry -> {
                        final Object value = entry.getValue().getValue();
                        final List<String> annotationAccessTypes = (value instanceof Collection)
                                ? ((Collection<?>) value).stream()
                                        .map(attr -> {
                                            // Java 11 and Java 17 behave differently (different impls)
                                            final String attrValue = attr.toString();
                                            final int classStartIndex = attrValue.lastIndexOf('.');
                                            return (classStartIndex == -1)
                                                    ? attrValue
                                                    : attrValue.substring(classStartIndex + 1);
                                        }).collect(Collectors.toList())
                                : List.of(value.toString());

                        return annotationAccessTypes.equals(accessTypeNames);
                    });
        };
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        try {
            final Set<TypeElement> types = getAnnotatedTypeElements(roundEnv);
            final List<Access> accesses = types.stream()
                    .flatMap(element -> getGraalAccessForAnnotatedElement(element).stream())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            final String configJson = getAccessConfigJson(accesses);
            final HintOrigin origin = HintUtils.getHintOrigin(roundEnv, processingEnv);
            final HintFile file = origin.getFileWithRelativePath(getFileName());
            return HintUtils.writeConfigFile(file, configJson, processingEnv);
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String getAccessConfigJson(Collection<Access> accesses) {
        return accesses.stream()
                .sorted(Comparator.comparing(a -> a.typeName))
                .map(AbstractAccessHintProcessor::getGraalReflectionForTypeName)
                .map(AbstractAccessHintProcessor::mapToJson)
                .collect(Collectors.joining(",\n", "[", "]"));
    }

    private static String mapToJson(Map<String, Object> map) {
        return map.entrySet().stream()
                .map(e -> (e.getValue() instanceof String)
                        ? String.format("\"%s\": \"%s\"", e.getKey(), e.getValue())
                        : String.format("\"%s\": %s", e.getKey(), e.getValue()))
                .collect(Collectors.joining(",\n  ", "{\n  ", "\n}"));
    }

    private static Map<String, Object> getGraalReflectionForTypeName(Access access) {
        final Map<String, Object> reflectionMap = new LinkedHashMap<>(access.accessTypes.length + 3);
        final String typeFinalName = isTypeInnerClass(access.typeName)
                ? getInnerTypeName(access.typeName)
                : access.typeName;

        reflectionMap.put(NAME, typeFinalName);
        Arrays.stream(access.accessTypes)
                .flatMap(accessType -> getGraalAccessType(accessType).stream())
                .distinct()
                .sorted()
                .forEach(graalAccessType -> reflectionMap.put(graalAccessType, true));

        return reflectionMap;
    }

    private static boolean isTypeInnerClass(String typeName) {
        return typeName.endsWith(".class");
    }

    private static String getInnerTypeName(String typeName) {
        final List<String> classType = new ArrayList<>();

        String packagePrefix = typeName.substring(0, typeName.length() - 6);
        int nextSeparator;
        while ((nextSeparator = packagePrefix.lastIndexOf('.')) != -1) {
            final String nextClassType = packagePrefix.substring(nextSeparator + 1);
            if (!Character.isUpperCase(nextClassType.charAt(0))) {
                break;
            }

            classType.add(nextClassType);
            packagePrefix = packagePrefix.substring(0, nextSeparator);
        }

        Collections.reverse(classType);
        return classType.stream().sequential().collect(Collectors.joining("$", packagePrefix + ".", ""));
    }

    private static List<String> getGraalAccessType(AccessType accessType) {
        switch (accessType) {
            case ALL_PUBLIC:
                return List.of(ALL_PUBLIC_CONSTRUCTORS, ALL_PUBLIC_METHODS, ALL_PUBLIC_FIELDS);
            case ALL_PUBLIC_CONSTRUCTORS:
                return List.of(ALL_PUBLIC_CONSTRUCTORS);
            case ALL_PUBLIC_METHODS:
                return List.of(ALL_PUBLIC_METHODS);
            case ALL_PUBLIC_FIELDS:
                return List.of(ALL_PUBLIC_FIELDS);

            case ALL_DECLARED:
                return List.of(ALL_DECLARED_CONSTRUCTORS, ALL_DECLARED_METHODS, ALL_DECLARED_FIELDS);
            case ALL_DECLARED_CONSTRUCTORS:
                return List.of(ALL_DECLARED_CONSTRUCTORS);
            case ALL_DECLARED_METHODS:
                return List.of(ALL_DECLARED_METHODS);
            case ALL_DECLARED_FIELDS:
                return List.of(ALL_DECLARED_FIELDS);
            default:
                throw new IllegalStateException("Unknown AccessType is present: " + accessType);
        }
    }
}
