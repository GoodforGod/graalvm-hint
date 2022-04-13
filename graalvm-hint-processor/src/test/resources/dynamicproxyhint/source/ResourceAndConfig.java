package io.goodforgod.graalvm.hint.processor;

import io.goodforgod.graalvm.hint.annotation.DynamicProxyHint;

@DynamicProxyHint(resources = {"res1", "res2"},
        files = {"file1"},
        value = {
                @DynamicProxyHint.Configuration(interfaces = {OptionParser.class, HintOrigin.class}),
                @DynamicProxyHint.Configuration(interfaces = {HintOrigin.class})
        })
public class ResourceAndConfig {

}
