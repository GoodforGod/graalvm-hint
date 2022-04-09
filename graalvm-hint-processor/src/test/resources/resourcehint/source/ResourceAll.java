package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.ResourceHint;

@ResourceHint(
        include = { "simplelogger.properties", "application.yml" },
        exclude = {"*.xml"},
        bundles = {"your.pkg.Bundle"}
)
public class ResourceAll {

}
