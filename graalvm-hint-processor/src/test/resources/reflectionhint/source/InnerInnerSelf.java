package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.ReflectionHint;


public class InnerInnerSelf {

    public static class InnerClass {

        @ReflectionHint
        public static class Example1 {

        }
    }
}
