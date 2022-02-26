package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.InitializationHint;
import io.goodforgod.graalvm.hint.annotation.NativeImageHint;
import io.goodforgod.graalvm.hint.annotation.NativeImageOptions;

@NativeImageHint(entrypoint = Entrypoint.class, name = "myapp", options = NativeImageOptions.PRINT_INITIALIZATION)
@InitializationHint(value = InitializationHint.InitPhase.BUILD, types = HintOptions.class)
@InitializationHint(value = InitializationHint.InitPhase.RUNTIME, typeNames = "io.goodforgod.graalvm.hint.processor")
public class Entrypoint {

    public static void main(String[] args) {}
}
