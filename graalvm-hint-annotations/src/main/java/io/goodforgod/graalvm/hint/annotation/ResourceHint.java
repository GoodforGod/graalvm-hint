package io.goodforgod.graalvm.hint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate which resources should be included/excluded when building native-image.
 *
 * @see <a href= "https://www.graalvm.org/reference-manual/native-image/Resources/">GraalVM Info</a>
 * @author Anton Kurako (GoodforGod)
 * @since 26.09.2021
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface ResourceHint {

    /**
     * By default, the native-image tool will not integrate any of the resources which are on the
     * classpath during the generation into the final image. To make calls such as Class.getResource()
     * or Class.getResourceAsStream() (or their corresponding ClassLoader methods) return specific
     * resources (instead of null), you must specify the resources that should be accessible at run
     * time.
     * Example: @ResourceHint(include = { "simplelogger.properties", ".*yaml" })
     *
     * @return resource patterns specified with Java regexp to Include during native-image generation
     *             into the final application.
     */
    String[] include() default {};

    /**
     * Example: @ResourceHint(exclude = { "simplelogger.properties", ".*yaml" })
     *
     * @return resource patterns specified with Java regexp to Exclude during native-image generation
     *             into the final application.
     */
    String[] exclude() default {};

    /**
     * Java localization support (java.util.ResourceBundle) enables Java code to load L10N resources and
     * show the right user messages suitable for actual runtime settings like time locale and format,
     * etc.
     * Example: @ResourceHint(bundles = { "your.pkg.Bundle", "another.pkg.Resource" })
     *
     * @return bundle names to include during native-image generation into the final application.
     */
    String[] bundles() default {};
}
