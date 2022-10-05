package io.goodforgod.graalvm.hint.processor;

import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 09.04.2022
 */
final class HintUtils {

    private HintUtils() {}

    static HintOrigin getHintOrigin(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
        final Map<String, String> options = processingEnv.getOptions();
        final Set<? extends Element> rootElements = roundEnv.getRootElements();
        final Element anyElement = rootElements.iterator().next();

        final String group = (options.containsKey(HintOrigin.HINT_PROCESSING_GROUP))
                ? options.get(HintOrigin.HINT_PROCESSING_GROUP)
                : getPackage(processingEnv, anyElement);

        final String artifact = (options.containsKey(HintOrigin.HINT_PROCESSING_ARTIFACT))
                ? options.get(HintOrigin.HINT_PROCESSING_ARTIFACT)
                : getArtifact(anyElement);

        return new HintOrigin(group, artifact);
    }

    static Set<TypeElement> getAnnotatedElements(RoundEnvironment roundEnv, Class<? extends Annotation>... annotations) {
        return Arrays.stream(annotations)
                .flatMap(a -> {
                    final Set<? extends Element> annotated = roundEnv.getElementsAnnotatedWith(a);
                    if (annotated == null || annotated.isEmpty())
                        return Stream.empty();

                    return ElementFilter.typesIn(annotated).stream();
                })
                .collect(Collectors.toSet());
    }

    static Optional<String> getAnnotationFieldClassNameAny(TypeElement type,
                                                           Class<? extends Annotation> annotation,
                                                           String annotationFieldName) {
        final List<String> classNames = getAnnotationFieldClassNames(type, annotation, annotationFieldName);
        return classNames.isEmpty()
                ? Optional.empty()
                : Optional.of(classNames.get(0));
    }

    static List<String> getAnnotationFieldClassNames(TypeElement type,
                                                     Class<? extends Annotation> annotation,
                                                     String annotationFieldName) {
        final String annotationName = annotation.getSimpleName();
        return type.getAnnotationMirrors().stream()
                .filter(a -> a.getAnnotationType().asElement().getSimpleName().contentEquals(annotationName))
                .flatMap(a -> HintUtils.getAnnotationFieldValues(a, annotationFieldName).stream())
                .collect(Collectors.toList());
    }

    static List<String> getAnnotationFieldValuesOrDefault(AnnotationMirror mirror,
                                                          String annotationFieldName,
                                                          List<String> defaultValues) {
        final List<String> fieldValues = getAnnotationFieldValues(mirror, annotationFieldName);
        return (fieldValues.isEmpty())
                ? defaultValues
                : fieldValues;
    }

    static List<String> getAnnotationFieldValues(AnnotationMirror mirror,
                                                 String annotationFieldName) {
        return mirror.getElementValues().entrySet().stream()
                .filter(e -> e.getKey().getSimpleName().contentEquals(annotationFieldName))
                .flatMap(e -> {
                    final Object value = e.getValue().getValue();
                    if (value instanceof Collection) {
                        return ((Collection<?>) value).stream().map(v -> (v instanceof AnnotationValue)
                                ? ((AnnotationValue) v).getValue().toString()
                                : v.toString());
                    } else if (value instanceof AnnotationValue) {
                        return Stream.of(((AnnotationValue) value).getValue().toString());
                    }

                    return Stream.of(value.toString());
                })
                .filter(e -> !e.isBlank())
                .collect(Collectors.toList());
    }

    static boolean writeConfigFile(HintFile file, String data, ProcessingEnvironment processingEnv) {
        try {
            final FileObject fileObject = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "",
                    file.getPath());
            try (Writer writer = fileObject.openWriter()) {
                writer.write(data);
            }
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Couldn't write GraalVM Hint " + file.getName() + " to due to: " + e.getMessage());
            return false;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                "Generating GraalVM Hint " + file.getName() + " to: " + file.getPath());
        return true;
    }

    private static String getPackage(ProcessingEnvironment processingEnv, Element element) {
        final PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(element);
        if (packageElement == null || packageElement.isUnnamed()) {
            return HintOrigin.DEFAULT_PACKAGE;
        }

        final String packageName = packageElement.getQualifiedName().toString();
        if (packageName.isEmpty()) {
            return HintOrigin.DEFAULT_PACKAGE;
        }

        return packageName;
    }

    private static String getArtifact(Element element) {
        return HintOrigin.DEFAULT_ARTIFACT;
    }
}
