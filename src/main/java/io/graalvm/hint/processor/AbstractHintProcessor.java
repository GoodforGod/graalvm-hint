package io.graalvm.hint.processor;

import java.util.Map;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;

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
