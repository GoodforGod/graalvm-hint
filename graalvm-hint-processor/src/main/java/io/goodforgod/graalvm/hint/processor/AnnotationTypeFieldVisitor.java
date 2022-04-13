package io.goodforgod.graalvm.hint.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 10.10.2021
 */
final class AnnotationTypeFieldVisitor implements AnnotationValueVisitor<List<String>, Void> {

    private final String annotationName;
    private final String annotationFieldName;

    AnnotationTypeFieldVisitor(String annotationName, String annotationFieldName) {
        this.annotationName = annotationName;
        this.annotationFieldName = annotationFieldName;
    }

    @Override
    public List<String> visit(AnnotationValue av, Void unused) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitBoolean(boolean b, Void unused) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitByte(byte b, Void unused) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitChar(char c, Void unused) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitDouble(double d, Void unused) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitFloat(float f, Void unused) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitInt(int i, Void unused) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitLong(long i, Void unused) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitShort(short s, Void unused) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitString(String s, Void unused) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitType(TypeMirror t, Void unused) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitEnumConstant(VariableElement c, Void unused) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitAnnotation(AnnotationMirror a, Void unused) {
        if (a.getAnnotationType().asElement().getSimpleName().contentEquals(annotationName)) {
            return a.getElementValues().entrySet().stream()
                    .filter(e -> e.getKey().getSimpleName().contentEquals(annotationFieldName))
                    .flatMap(e -> {
                        final Object value = e.getValue().getValue();
                        return (value instanceof Collection)
                                ? ((Collection<?>) value).stream().map(Object::toString)
                                : Stream.of(value.toString());
                    })
                    .filter(e -> !e.isBlank())
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> visitArray(List<? extends AnnotationValue> vals, Void unused) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitUnknown(AnnotationValue av, Void unused) {
        return Collections.emptyList();
    }
}
