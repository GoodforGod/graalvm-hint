package io.goodforgod.graalvm.hint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Java dynamic proxies, implemented by java.lang.reflect.Proxy, provide a mechanism which enables
 * object level access control by routing all method invocations through
 * java.lang.reflect.InvocationHandler
 *
 * @see <a href="https://www.graalvm.org/reference-manual/native-image/DynamicProxy/">GraalVM
 *          Info</a>
 * @author Anton Kurako (GoodforGod)
 * @since 07.04.2022
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
public @interface DynamicProxyHint {

    @Target({})
    @Retention(RetentionPolicy.SOURCE)
    @interface Configuration {

        /**
         * @return The interfaces to provide a hint (preferred because typesafe)
         */
        Class<?>[] interfaces();
    }

    /**
     * @see <a href=
     *          "https://www.graalvm.org/22.0/reference-manual/native-image/DynamicProxy/#manual-configuration">GraalVM</a>
     * @return Manual configurations for Dynamic proxy
     */
    Configuration[] value() default {};

    /**
     * @return file configs to include under -H:DynamicProxyConfigurationFiles option
     */
    String[] files() default {};

    /**
     * @return resources configs to include under -H:DynamicProxyConfigurationResources option
     */
    String[] resources() default {};
}
