package io.goodforgod.graalvm.hint.processor;

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
class AnnotationTypeFieldVisitor implements AnnotationValueVisitor<List<String>, String> {

    private final String annotationName;
    private final String annotationFieldName;

    public AnnotationTypeFieldVisitor(String annotationName, String annotationFieldName) {
        this.annotationName = annotationName;
        this.annotationFieldName = annotationFieldName;
    }

    @Override
    public List<String> visit(AnnotationValue av, String s) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitBoolean(boolean b, String s) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitByte(byte b, String s) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitChar(char c, String s) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitDouble(double d, String s) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitFloat(float f, String s) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitInt(int i, String s) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitLong(long i, String s) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitShort(short s, String s2) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitString(String s, String s2) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitType(TypeMirror t, String s) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitEnumConstant(VariableElement c, String s) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitAnnotation(AnnotationMirror a, String s) {
        if (a.getAnnotationType().asElement().getSimpleName().contentEquals(annotationName)) {
            return a.getElementValues().entrySet().stream()
                    .filter(e -> e.getKey().getSimpleName().contentEquals(annotationFieldName))
                    .flatMap(e -> {
                        final Object value = e.getValue().getValue();
                        return (value instanceof List)
                                ? ((List<?>) value).stream().map(Object::toString)
                                : Stream.of(value.toString());
                    })
                    .filter(e -> !e.isBlank())
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> visitArray(List<? extends AnnotationValue> vals, String s) {
        return Collections.emptyList();
    }

    @Override
    public List<String> visitUnknown(AnnotationValue av, String s) {
        return Collections.emptyList();
    }
}
