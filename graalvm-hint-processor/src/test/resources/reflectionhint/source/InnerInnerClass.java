package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.ReflectionHint;
import io.goodforgod.graalvm.hint.processor.InnerInnerClass.InnerClass.Example1;


@ReflectionHint(types = Example1.class)
public class InnerInnerClass {

    public static class InnerClass {

        public static class Example1 {

        }
    }
}
