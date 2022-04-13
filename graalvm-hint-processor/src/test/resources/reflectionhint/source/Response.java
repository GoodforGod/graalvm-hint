package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.ReflectionHint;

@ReflectionHint(value = ReflectionHint.AccessType.ALL_DECLARED)
@ReflectionHint(value = ReflectionHint.AccessType.ALL_DECLARED_FIELDS, types = HintOrigin.class)
@ReflectionHint(value = ReflectionHint.AccessType.ALL_PUBLIC_METHODS, typeNames = "io.goodforgod.graalvm.hint.processor")
public class Response {

    private String name;
}
