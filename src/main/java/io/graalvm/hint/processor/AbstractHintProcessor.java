package io.graalvm.hint.processor;

import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 29.09.2021
 */
abstract class AbstractHintProcessor extends AbstractProcessor {

    private static final String DEFAULT_GROUP = "io.graalvm.hint";
    private static final String DEFAULT_ARTIFACT = "application";

    HintOptions getHintOptions(RoundEnvironment roundEnv) {
        final Map<String, String> options = processingEnv.getOptions();
        final Element anyElement = roundEnv.getRootElements().iterator().next();

        final String group = (options.containsKey(HintOptions.HINT_PROCESSING_GROUP))
                ? options.get(HintOptions.HINT_PROCESSING_GROUP)
                : getPackage(anyElement);

        final String artifact = (options.containsKey(HintOptions.HINT_PROCESSING_ARTIFACT))
                ? options.get(HintOptions.HINT_PROCESSING_ARTIFACT)
                : getArtifact(anyElement);

        return new HintOptions(group, artifact);
    }

    Optional<String> getAnnotationFieldClassNameAny(TypeElement type,
                                                    String annotationSimpleName,
                                                    String annotationFieldName) {
        final List<String> classNames = getAnnotationFieldClassNames(type, annotationSimpleName, annotationFieldName);
        return classNames.isEmpty()
                ? Optional.empty()
                : Optional.of(classNames.get(0));
    }

    List<String> getAnnotationFieldClassNames(TypeElement type,
                                              String annotationSimpleName,
                                              String annotationFieldName) {
        return type.getAnnotationMirrors().stream()
                .filter(a -> a.getAnnotationType().asElement().getSimpleName().contentEquals(annotationSimpleName))
                .flatMap(a -> a.getElementValues().entrySet().stream()
                        .filter(e -> e.getKey().getSimpleName().contentEquals(annotationFieldName))
                        .flatMap(e -> {
                            final Object value = e.getValue().getValue();
                            return (e instanceof List)
                                    ? ((List<?>) e).stream().map(Object::toString)
                                    : Stream.of(value.toString());
                        })
                        .filter(e -> !e.isBlank()))
                .collect(Collectors.toList());
    }

    public boolean writeConfigFile(String fileName,
                                   String data,
                                   RoundEnvironment roundEnv) {
        final HintOptions hintOptions = getHintOptions(roundEnv);
        final String path = hintOptions.getRelativePathForFile(fileName);
        try {
            final FileObject fileObject = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", path);
            try (Writer writer = fileObject.openWriter()) {
                writer.write(data);
            }
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Couldn't write " + fileName + " due to: " + e.getMessage());
            return false;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generated " + fileName + " file to: " + path);
        return true;
    }

    private String getPackage(Element element) {
        final Element enclosingElement = element.getEnclosingElement();
        if (enclosingElement instanceof PackageElement) {
            final String typeName = ((PackageElement) enclosingElement).getQualifiedName().toString();
            return (typeName.contains("."))
                    ? typeName.substring(0, typeName.lastIndexOf('.'))
                    : typeName;
        } else {
            return DEFAULT_GROUP;
        }
    }

    private String getArtifact(Element element) {
        final Element enclosingElement = element.getEnclosingElement();
        if (enclosingElement instanceof PackageElement) {
            final String typeName = ((PackageElement) enclosingElement).getQualifiedName().toString();
            return (typeName.contains("."))
                    ? typeName.substring(typeName.lastIndexOf('.') + 1)
                    : DEFAULT_ARTIFACT;
        } else {
            return DEFAULT_ARTIFACT;
        }
    }
}
