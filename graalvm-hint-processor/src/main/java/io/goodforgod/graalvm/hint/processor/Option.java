package io.goodforgod.graalvm.hint.processor;

import java.util.List;
import java.util.Objects;

/**
 * Anton Kurako (GoodforGod)
 *
 * @since 20.02.2024
 */
final class Option {

    private final HintOrigin origin;
    private final List<String> options;

    public Option(HintOrigin origin, List<String> options) {
        this.origin = origin;
        this.options = options;
    }

    public HintOrigin getOrigin() {
        return origin;
    }

    public List<String> getOptions() {
        return options;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Option option = (Option) o;
        return Objects.equals(origin, option.origin) && Objects.equals(options, option.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, options);
    }

    @Override
    public String toString() {
        return "[path=" + origin + ", options=" + options + ']';
    }
}
