package io.goodforgod.graalvm.hint.processor;

import static io.goodforgod.graalvm.hint.processor.InnerClass.*;

import io.goodforgod.graalvm.hint.annotation.ReflectionHint;

@ReflectionHint(types = Example1.class)
public class InnerClass {

    public static class Example1 {}
}
