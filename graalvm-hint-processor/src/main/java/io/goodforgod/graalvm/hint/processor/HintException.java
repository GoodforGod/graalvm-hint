package io.goodforgod.graalvm.hint.processor;

/**
 * Manageable annotation exception
 *
 * @author Anton Kurako (GoodforGod)
 * @since 09.04.2022
 */
final class HintException extends RuntimeException {

    HintException(String message) {
        super(message);
    }
}
