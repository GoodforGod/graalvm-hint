package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.ResourceHint;

@ResourceHint(patterns = { "simplelogger.properties", "application.yml" })
public class ResourceNames {

    private String name;
}
