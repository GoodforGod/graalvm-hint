package io.goodforgod.graalvm.hint.annotation;

import java.lang.annotation.*;

/**
 * Allow for repeatable annotations {@link TypeHint}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 10.10.2021
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface TypeHints {

    /**
     * @return repeatable hint annotations
     */
    TypeHint[] value();
}
