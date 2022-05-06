package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.LinkHint;
import io.goodforgod.graalvm.hint.annotation.NativeImageHint;
import io.goodforgod.graalvm.hint.annotation.NativeImageOptions;

@NativeImageHint(entrypoint = EntrypointAndLink.class, name = "myapp", options = NativeImageOptions.PRINT_INITIALIZATION)
@LinkHint(types = {HintOrigin.class, EntrypointAndLink.class})
public class EntrypointAndLink {

    public static void main(String[] args) {}
}
