package io.goodforgod.graalvm.hint.processor;

import java.lang.annotation.Annotation;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

/**
 * Contract for hint parser that produces options
 *
 * @author Anton Kurako (GoodforGod)
 * @since 07.04.2022
 */
interface OptionParser {

    List<Class<? extends Annotation>> annotations();

    List<String> getOptions(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv);
}
