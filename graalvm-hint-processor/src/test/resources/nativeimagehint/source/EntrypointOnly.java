package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.NativeImageHint;

@NativeImageHint(entrypoint = EntrypointOnly.class)
public class EntrypointOnly {

    public static void main(String[] args) {}
}
