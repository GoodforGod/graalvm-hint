package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.JniHint;

@JniHint(value = { JniHint.AccessType.ALL_DECLARED_CONSTRUCTORS,
        JniHint.AccessType.ALL_DECLARED_FIELDS,
        JniHint.AccessType.ALL_DECLARED_METHODS,
        JniHint.AccessType.ALL_PUBLIC_CONSTRUCTORS,
        JniHint.AccessType.ALL_PUBLIC_FIELDS })
public class RequestOnlyManyAccess {

    private String name;
}
