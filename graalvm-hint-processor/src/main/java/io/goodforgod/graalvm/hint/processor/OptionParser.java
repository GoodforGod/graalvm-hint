package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.NativeImageHint;
import java.lang.annotation.Annotation;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

/**
 * Hint parser that produces options for {@link NativeImageHint#optionNames()}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 07.04.2022
 */
interface OptionParser {

    /**
     * @return annotation parser supports
     */
    List<Class<? extends Annotation>> getSupportedAnnotations();

    /**
     * @param roundEnv      parser invoked in
     * @param processingEnv parser invoked in
     * @return list of {@link NativeImageHint#optionNames()} to include
     */
    List<Option> getOptions(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv);
}
