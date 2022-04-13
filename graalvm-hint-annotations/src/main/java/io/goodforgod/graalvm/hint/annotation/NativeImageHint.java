package io.goodforgod.graalvm.hint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation is responsible for generating native-image.properties file for GraalVM
 *
 * @author Anton Kurako (GoodforGod)
 * @since 30.09.2021
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface NativeImageHint {

    /**
     * @return class that is entrypoint for native application with main() method.
     */
    Class<?> entrypoint() default Void.class;

    /**
     * @return final name of native application after native-image generation.
     */
    String name() default "";

    /**
     * @return additional options to include for native-image.properties file
     */
    NativeImageOptions[] options() default {};

    /**
     * @see #options()
     * @return additional options to include for native-image.properties file as string for some
     *             non-standard options.
     */
    String[] optionNames() default {};
}
