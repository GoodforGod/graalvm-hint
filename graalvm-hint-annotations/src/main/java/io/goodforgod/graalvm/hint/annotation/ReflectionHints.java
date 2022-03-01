package io.goodforgod.graalvm.hint.annotation;

import java.lang.annotation.*;

/**
 * Allow for repeatable annotations {@link ReflectionHint}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 10.10.2021
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface ReflectionHints {

    /**
     * @return repeatable hint annotations
     */
    ReflectionHint[] value();
}
