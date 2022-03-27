package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.JniHint;

@JniHint(value = JniHint.AccessType.ALL_DECLARED)
@JniHint(value = JniHint.AccessType.ALL_DECLARED_FIELDS, types = HintOptions.class)
@JniHint(value = JniHint.AccessType.ALL_PUBLIC_METHODS, typeNames = "io.goodforgod.graalvm.hint.processor")
public class Response {

    private String name;
}
