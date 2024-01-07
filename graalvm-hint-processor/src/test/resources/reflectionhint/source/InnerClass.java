package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.ReflectionHint;

import static io.goodforgod.graalvm.hint.processor.InnerClass.Example1;

@ReflectionHint(types = Example1.class)
public class InnerClass {

    public static class Example1 { }
}
