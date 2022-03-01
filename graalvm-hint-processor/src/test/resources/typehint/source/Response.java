package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.ReflectionHint;

@ReflectionHint(value = { ReflectionHint.AccessType.ALL_DECLARED })
@ReflectionHint(types = HintOptions.class, value = ReflectionHint.AccessType.ALL_DECLARED_FIELDS)
@ReflectionHint(typeNames = "io.goodforgod.graalvm.hint.processor")
public class Response {

    private String name;
}
