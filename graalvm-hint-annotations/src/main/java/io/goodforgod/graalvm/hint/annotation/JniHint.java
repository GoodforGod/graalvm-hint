package io.goodforgod.graalvm.hint.annotation;

import java.lang.annotation.*;

/**
 * The JNI hint annotation is a GraalVM annotation that is used on interfaces to provide
 * additional
 * information about types used at runtime for Java Native Interface access.
 *
 * @see <a href="https://www.graalvm.org/reference-manual/native-image/JNI/">GraalVM Info</a>
 * @author Anton Kurako (GoodforGod)
 * @since 21.03.2022
 */
@Repeatable(JniHints.class)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
public @interface JniHint {

    /**
     * Describes the JNI access for types.
     *
     * @return The JNI access type
     */
    AccessType[] value() default { AccessType.ALL_DECLARED };

    /**
     * @return The types to provide a hint (preferred because typesafe)
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
     * The JNI access type.
     */
    enum AccessType {

        /**
         * All public members.
         */
        ALL_PUBLIC,

        /**
         * All public constructors require access.
         */
        ALL_PUBLIC_CONSTRUCTORS,

        /**
         * All public methods require access.
         */
        ALL_PUBLIC_METHODS,

        /**
         * All public fields require access.
         */
        ALL_PUBLIC_FIELDS,

        /**
         * All public declared members.
         */
        ALL_DECLARED,

        /**
         * All declared constructors require access.
         */
        ALL_DECLARED_CONSTRUCTORS,

        /**
         * All declared methods require access.
         */
        ALL_DECLARED_METHODS,

        /**
         * All declared fields require access.
         */
        ALL_DECLARED_FIELDS
    }
}
