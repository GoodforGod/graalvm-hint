package io.goodforgod.graalvm.hint.processor;

import javax.lang.model.element.Element;

/**
 * Manageable annotation exception
 *
 * @author Anton Kurako (GoodforGod)
 * @since 09.04.2022
 */
final class HintException extends RuntimeException {

    private final Element element;

    HintException(String message, Element element) {
        super(message);
        this.element = element;
    }

    public Element getElement() {
        return element;
    }
}
