package io.goodforgod.graalvm.hint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @see <a href="https://www.graalvm.org/reference-manual/native-image/DynamicProxy/">GraalVM
 *          Info</a>
 * @author Anton Kurako (GoodforGod)
 * @since 07.04.2022
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
public @interface DynamicProxyHint {

    @Target({ ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.SOURCE)
    @interface DynamicProxyConfiguration {

        /**
         * @return The interfaces to provide a hint (preferred because typesafe)
         */
        Class[] interfaces() default {};

        /**
         * Alternative way to configure Interface names, should be used when type visibility
         * prevents using {@link #interfaces()} references.
         *
         * @return the interface names
         */
        String[] interfaceNames() default {};
    }

    DynamicProxyConfiguration[] configurations() default {};

    String[] files() default {};

    String[] resources() default {};
}
