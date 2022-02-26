package io.goodforgod.graalvm.hint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate which resources should be pulled into the image. Resources are described by patterns.
 *
 * @see <a href= "https://www.graalvm.org/reference-manual/native-image/Resources/">GraalVM Info</a>
 * @author Anton Kurako (GoodforGod)
 * @since 26.09.2021
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface ResourceHint {

    /**
     * Example: @ResourceHint(patterns = { "simplelogger.properties", ".*yaml" })
     *
     * @return resource patterns specified with Java regexp for regular resources.
     */
    String[] patterns();
}
