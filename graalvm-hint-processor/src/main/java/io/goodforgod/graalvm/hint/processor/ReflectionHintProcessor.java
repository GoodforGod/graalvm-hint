package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.ReflectionHint;
import io.goodforgod.graalvm.hint.annotation.ReflectionHint.AccessType;
import io.goodforgod.graalvm.hint.annotation.ReflectionHints;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Processes {@link ReflectionHint} annotation for native-image reflect-config.json file
 *
 * @author Anton Kurako (GoodforGod)
 * @see ReflectionHint
 * @since 27.09.2021
 */
@SupportedAnnotationTypes({
        "io.goodforgod.graalvm.hint.annotation.ReflectionHint",
        "io.goodforgod.graalvm.hint.annotation.ReflectionHints"
})
@SupportedOptions({
        HintOptions.HINT_PROCESSING_GROUP,
        HintOptions.HINT_PROCESSING_ARTIFACT
})
public final class ReflectionHintProcessor extends AbstractHintProcessor {

    private static final String ALL_PUBLIC_CONSTRUCTORS = "allPublicConstructors";
    private static final String ALL_PUBLIC_FIELDS = "allPublicFields";
    private static final String ALL_PUBLIC_METHODS = "allPublicMethods";

    private static final String ALL_DECLARED_CONSTRUCTORS = "allDeclaredConstructors";
    private static final String ALL_DECLARED_FIELDS = "allDeclaredFields";
    private static final String ALL_DECLARED_METHODS = "allDeclaredMethods";

    private static final String FILE_NAME = "reflect-config.json";

    private static final String NAME = "name";

    static class Reflection implements Comparable<Reflection> {

        private final String targetName;
        private final AccessType[] accessTypes;

        Reflection(String targetName, AccessType[] accessTypes) {
            this.targetName = targetName;
            this.accessTypes = accessTypes;
        }

        @Override
        public int compareTo(Reflection o) {
            return targetName.compareTo(o.targetName);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Reflection that = (Reflection) o;
            return Objects.equals(targetName, that.targetName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(targetName);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        try {
            final Set<TypeElement> types = getAnnotatedElements(roundEnv, ReflectionHint.class, ReflectionHints.class);
            final List<Reflection> reflections = types.stream()
                    .flatMap(element -> getGraalReflectionsForAnnotatedElement(element).stream())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            final Optional<String> reflectionConfigJson = getReflectionConfigJson(reflections);
            if (reflectionConfigJson.isEmpty()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING,
                        "@ReflectionHint annotation found, but not reflection type hints parsed");
                return false;
            }

            return writeConfigFile(FILE_NAME, reflectionConfigJson.get(), roundEnv);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Optional<String> getReflectionConfigJson(Collection<Reflection> reflections) {
        if (reflections.isEmpty())
            return Optional.empty();

        return Optional.of(reflections.stream()
                .map(ReflectionHintProcessor::getGraalReflectionForTypeName)
                .map(ReflectionHintProcessor::mapToJson)
                .collect(Collectors.joining(",\n", "[", "]")));
    }

    private static String mapToJson(Map<String, Object> map) {
        return map.entrySet().stream()
                .map(e -> (e.getValue() instanceof String)
                        ? String.format("\"%s\": \"%s\"", e.getKey(), e.getValue())
                        : String.format("\"%s\": %s", e.getKey(), e.getValue()))
                .collect(Collectors.joining(",\n  ", "{\n  ", "\n}"));
    }

    private Collection<Reflection> getGraalReflectionsForAnnotatedElement(TypeElement element) {
        final ReflectionHints hints = element.getAnnotation(ReflectionHints.class);
        if (hints == null) {
            final ReflectionHint reflectionHint = element.getAnnotation(ReflectionHint.class);
            return getGraalReflectionsForAnnotatedElement(element, reflectionHint, false);
        } else {
            return Arrays.stream(hints.value())
                    .flatMap(hint -> getGraalReflectionsForAnnotatedElement(element, hint, true).stream())
                    .collect(Collectors.toList());
        }
    }

    private Collection<Reflection> getGraalReflectionsForAnnotatedElement(TypeElement element,
                                                                          ReflectionHint hint,
                                                                          boolean isParentAnnotation) {
        final AccessType[] accessTypes = hint.value();
        final List<String> typeNames = Arrays.asList(hint.typeNames());
        final List<String> types = (!isParentAnnotation)
                ? getAnnotationFieldClassNames(element, ReflectionHint.class, "types")
                : getAnnotationFieldClassNames(element, ReflectionHint.class, "types", ReflectionHints.class,
                        a -> ((AnnotationMirror) a).getElementValues().entrySet().stream()
                                .filter(e -> e.getKey().getSimpleName().contentEquals("value"))
                                .anyMatch(e -> {
                                    final Object value = e.getValue().getValue();
                                    final List<String> accessTypesReflection = (value instanceof Collection)
                                            ? ((Collection<?>) value).stream().map(Object::toString).collect(Collectors.toList())
                                            : List.of(value.toString());

                                    final List<String> accessTypeNames = Arrays.stream(accessTypes)
                                            .map(Enum::name)
                                            .collect(Collectors.toList());

                                    return accessTypesReflection.equals(accessTypeNames);
                                }));

        if (types.isEmpty() && typeNames.isEmpty()) {
            final String selfName = element.getQualifiedName().toString();
            return List.of(new Reflection(selfName, accessTypes));
        }

        return Stream.concat(types.stream(), typeNames.stream())
                .map(t -> new Reflection(t, accessTypes))
                .collect(Collectors.toList());
    }

    private static Map<String, Object> getGraalReflectionForTypeName(Reflection reflection) {
        final Map<String, Object> reflectionMap = new LinkedHashMap<>(reflection.accessTypes.length + 3);
        final String typeFinalName = isTypeInnerClass(reflection.targetName)
                ? getInnerTypeName(reflection.targetName)
                : reflection.targetName;

        reflectionMap.put(NAME, typeFinalName);
        Arrays.stream(reflection.accessTypes)
                .flatMap(t -> getGraalAccessType(t).stream())
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
