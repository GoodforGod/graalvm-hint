package io.goodforgod.graalvm.hint.annotation;

import java.lang.annotation.*;

/**
 * Indicate which types to be fully defined at image build-time.
 *
 * @see <a href=
 *          "https://www.graalvm.org/22.1/reference-manual/native-image/Options/#options-to-native-image-builder">GraalVM
 *          Documentation</a>
 * @see <a href="https://github.com/oracle/graal/pull/4305">Github Issue</a>
 * @author Anton Kurako (GoodforGod)
 * @since 02.05.2022
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
public @interface LinkHint {

    /**
     * @return true if all classes in scope of the option are required to be fully defined.
     */
    boolean all() default false;

    /**
     * @return The types to link at build time (preferred because typesafe)
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
}
