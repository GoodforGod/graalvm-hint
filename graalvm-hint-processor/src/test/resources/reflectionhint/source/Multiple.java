package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.ReflectionHint;

@ReflectionHint(
        value = { ReflectionHint.AccessType.ALL_DECLARED_FIELDS },
        types = { Multiple.Example1.class, Multiple.Example2.class },
        typeNames = { "org.simpleframework.xml.core.ElementLabel" })
@ReflectionHint(types = { Multiple.Example3.class, Multiple.Example4.class })
public class Multiple {

    public static class Example1 { }

    public static class Example2 { }

    public static class Example3 { }

    public static class Example4 { }
}
