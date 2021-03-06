package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.InitializationHint;
import io.goodforgod.graalvm.hint.annotation.NativeImageHint;
import io.goodforgod.graalvm.hint.annotation.NativeImageOptions;

@NativeImageHint(entrypoint = Entrypoint.class, name = "myapp", options = NativeImageOptions.PRINT_INITIALIZATION)
public class Entrypoint {

    public static void main(String[] args) {}
}
