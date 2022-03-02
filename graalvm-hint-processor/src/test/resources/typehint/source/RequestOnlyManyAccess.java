package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.ReflectionHint;

@ReflectionHint(value = { ReflectionHint.AccessType.ALL_DECLARED_CONSTRUCTORS,
        ReflectionHint.AccessType.ALL_DECLARED_FIELDS,
        ReflectionHint.AccessType.ALL_DECLARED_METHODS,
        ReflectionHint.AccessType.ALL_PUBLIC_CONSTRUCTORS,
        ReflectionHint.AccessType.ALL_PUBLIC_FIELDS })
public class RequestOnlyManyAccess {

    private String name;
}
