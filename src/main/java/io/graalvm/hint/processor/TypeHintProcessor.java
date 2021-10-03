package io.graalvm.hint.processor;

import io.graalvm.hint.annotation.TypeHint;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
 * @see TypeHint
 * @since 27.09.2021
 */
@SupportedAnnotationTypes("io.graalvm.hint.annotation.TypeHint")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedOptions({
        HintOptions.HINT_PROCESSING_GROUP,
        HintOptions.HINT_PROCESSING_ARTIFACT
})
public class TypeHintProcessor extends AbstractHintProcessor {

    private static final String ALL_PUBLIC = "allPublic";
    private static final String ALL_PUBLIC_CONSTRUCTORS = "allPublicConstructors";
    private static final String ALL_PUBLIC_FIELDS = "allPublicFields";
    private static final String ALL_PUBLIC_METHODS = "allPublicMethods";

    private static final String ALL_DECLARED = "allDeclared";
    private static final String ALL_DECLARED_CONSTRUCTORS = "allDeclaredConstructors";
    private static final String ALL_DECLARED_FIELDS = "allDeclaredFields";
    private static final String ALL_DECLARED_METHODS = "allDeclaredFields";

    private static final String FILE_NAME = "reflect-config.json";

    private static final String NAME = "name";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        final Set<? extends Element> annotated = roundEnv.getElementsAnnotatedWith(TypeHint.class);
        final Set<TypeElement> types = ElementFilter.typesIn(annotated);

        final List<Map<String, Object>> reflections = types.stream()
                .flatMap(element -> getGraalReflectionsForAnnotatedElement(element).stream())
                .collect(Collectors.toList());

        final Optional<String> reflectionConfigJson = getReflectionConfigJson(reflections);
        if (reflectionConfigJson.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING,
                    "@TypeHint found, but not reflection type hints parsed");
            return false;
        }

        return writeConfigFile(FILE_NAME, reflectionConfigJson.get(), roundEnv);
    }

    private Optional<String> getReflectionConfigJson(List<Map<String, Object>> reflections) {
        if (reflections.isEmpty())
            return Optional.empty();

        return Optional.of(reflections.stream()
                .map(TypeHintProcessor::mapToJson)
                .collect(Collectors.joining(",\n", "[", "]")));
    }

    private static String mapToJson(Map<String, Object> map) {
        return map.entrySet().stream()
                .map(e -> (e.getValue() instanceof String)
                        ? String.format("\"%s\": \"%s\"", e.getKey(), e.getValue())
                        : String.format("\"%s\": %s", e.getKey(), e.getValue()))
                .collect(Collectors.joining(",\n  ", "{\n  ", "\n}"));
    }

    private List<Map<String, Object>> getGraalReflectionsForAnnotatedElement(TypeElement element) {
        final TypeHint typeHint = element.getAnnotation(TypeHint.class);
        final TypeHint.AccessType[] accessTypes = typeHint.value();

        final List<String> types = getAnnotationFieldClassNames(element, TypeHint.class.getSimpleName(), "types");
        final List<String> typeNames = Arrays.asList(typeHint.typeNames());

        if (types.isEmpty() && typeNames.isEmpty()) {
            final String selfName = element.getQualifiedName().toString();
            final Map<String, Object> selfReflection = getGraalReflectionForTypeName(selfName, accessTypes);
            return List.of(selfReflection);
        }

        return Stream.concat(types.stream(), typeNames.stream())
                .distinct()
                .map(t -> getGraalReflectionForTypeName(t, accessTypes))
                .collect(Collectors.toList());
    }

    private static Map<String, Object> getGraalReflectionForTypeName(String typeName,
                                                                     TypeHint.AccessType[] accessTypes) {
        final Map<String, Object> reflection = new LinkedHashMap<>(accessTypes.length + 3);
        reflection.put(NAME, typeName);

        Arrays.stream(accessTypes)
                .sorted()
                .forEach(accessType -> {
                    final String graalAccessType = getGraalAccessType(accessType);
                    reflection.put(graalAccessType, true);
                });

        return reflection;
    }

    private static String getGraalAccessType(TypeHint.AccessType accessType) {
        switch (accessType) {
            case ALL_PUBLIC:
                return ALL_PUBLIC;
            case ALL_PUBLIC_CONSTRUCTORS:
                return ALL_PUBLIC_CONSTRUCTORS;
            case ALL_PUBLIC_FIELDS:
                return ALL_PUBLIC_FIELDS;
            case ALL_PUBLIC_METHODS:
                return ALL_PUBLIC_METHODS;

            case ALL_DECLARED:
                return ALL_DECLARED;
            case ALL_DECLARED_FIELDS:
                return ALL_DECLARED_FIELDS;
            case ALL_DECLARED_METHODS:
                return ALL_DECLARED_METHODS;
            case ALL_DECLARED_CONSTRUCTORS:
                return ALL_DECLARED_CONSTRUCTORS;
            default:
                throw new IllegalStateException("Unknown AccessType is present: " + accessType);
        }
    }
}
