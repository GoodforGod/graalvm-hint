package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.NativeImageHint;
import io.goodforgod.graalvm.hint.annotation.NativeImageOptions;

@NativeImageHint(options = NativeImageOptions.INLINE_BEFORE_ANALYSIS)
public class EntrypointOptions {

    public static void main(String[] args) {}
}
