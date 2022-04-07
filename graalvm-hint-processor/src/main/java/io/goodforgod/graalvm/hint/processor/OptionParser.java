package io.goodforgod.graalvm.hint.processor;

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

    List<String> getOptions(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv);
}
