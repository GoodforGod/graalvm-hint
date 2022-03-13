package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.InitializationHint;
import io.goodforgod.graalvm.hint.annotation.NativeImageHint;

@InitializationHint(value = InitializationHint.InitPhase.BUILD, types = HintOptions.class)
public class Build {

}
