package io.goodforgod.graalvm.hint.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * @author GoodforGod
 * @since 13.11.2019
 */
abstract class ProcessorRunner {

    String getResourceContentAsString(String name) {
        try (InputStream resourceAsStream = ProcessorRunner.class.getClassLoader().getResourceAsStream(name)) {
            if (resourceAsStream == null)
                return null;

            try (final InputStreamReader isr = new InputStreamReader(resourceAsStream);
                    final BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}
