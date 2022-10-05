package io.goodforgod.graalvm.hint.processor;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.lang.model.SourceVersion;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 29.09.2021
 */
abstract class AbstractHintProcessor extends AbstractProcessor {

    protected abstract Set<Class<? extends Annotation>> getSupportedAnnotations();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return getSupportedAnnotations().stream()
                .map(Class::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Set.of(HintOrigin.HINT_PROCESSING_GROUP, HintOrigin.HINT_PROCESSING_ARTIFACT);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        SourceVersion sourceVersion = SourceVersion.latest();
        if (sourceVersion.ordinal() <= 19) {
            if (sourceVersion.ordinal() >= 11) {
                return sourceVersion;
            } else {
                return SourceVersion.RELEASE_11;
            }
        } else {
            return SourceVersion.values()[19];
        }
    }
}
