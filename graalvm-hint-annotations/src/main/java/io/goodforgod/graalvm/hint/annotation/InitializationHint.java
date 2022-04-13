package io.goodforgod.graalvm.hint.annotation;

import java.lang.annotation.*;

/**
 * Indicate which classes/packages should be initialized explicitly at build-time or runtime.
 *
 * @see <a href="https://www.graalvm.org/reference-manual/native-image/ClassInitialization/">GraalVM
 *          Info</a>
 * @author Anton Kurako (GoodforGod)
 * @since 26.09.2021
 */
@Repeatable(InitializationHints.class)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
public @interface InitializationHint {

    /**
     * @return the initialization time, usually set to {@link InitPhase#BUILD} since runtime is GraalVM
     *             native image default.
     */
    InitPhase value() default InitPhase.BUILD;

    /**
     * @return The types to provide an initialization (preferred because typesafe)
     */
    Class[] types() default {};

    /**
     * Alternative way to configure, Type names or Package names, should be used when type visibility
     * prevents using {@link #types()} references, or for nested types which should be specified using a
     * {@code $} separator (for example {@code com.example.Foo$Bar}).
     *
     * @return the type names
     */
    String[] typeNames() default {};

    /**
     * Specifies the phase of initialization hint.
     */
    enum InitPhase {

        /**
         * Initialize during build phase of native-image.
         */
        BUILD,

        /**
         * Initialize during runtime phase (GraalVM default).
         */
        RUNTIME
    }
}
