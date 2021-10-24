package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.TypeHint;

@TypeHint(value = { TypeHint.AccessType.ALL_DECLARED })
@TypeHint(types = HintOptions.class, value = TypeHint.AccessType.ALL_DECLARED_FIELDS)
@TypeHint(typeNames = "io.goodforgod.graalvm.hint.processor")
public class Response {

    private String name;
}
