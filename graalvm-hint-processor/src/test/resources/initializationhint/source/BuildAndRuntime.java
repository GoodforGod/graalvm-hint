package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.InitializationHint;
import io.goodforgod.graalvm.hint.annotation.NativeImageHint;

@InitializationHint(value = InitializationHint.InitPhase.BUILD, types = HintOptions.class)
@InitializationHint(value = InitializationHint.InitPhase.RUNTIME, typeNames = "io.goodforgod.graalvm.hint.processor")
public class BuildAndRuntime {

}
