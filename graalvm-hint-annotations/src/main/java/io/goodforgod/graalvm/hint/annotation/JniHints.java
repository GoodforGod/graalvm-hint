package io.goodforgod.graalvm.hint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allow for repeatable annotations {@link JniHint}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 21.03.2022
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface JniHints {

    /**
     * @return repeatable hint annotations
     */
    JniHint[] value();
}
