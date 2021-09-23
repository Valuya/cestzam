package be.valuya.cestzam.api.client;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Arrays.asList;


public class CdiExtension implements TestInstancePostProcessor {

    private static final SeContainer CONTAINER = SeContainerInitializer
            .newInstance()
            .initialize();

    private static final Predicate<Annotation> IS_QUALIFIER = a -> a.annotationType().isAnnotationPresent(Qualifier.class);

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws IllegalAccessException {
        for (Field field : getFields(testInstance.getClass())) {
            if (field.getAnnotation(Inject.class) != null) {
                Annotation[] qualifiers = Arrays.stream(field.getAnnotations())
                        .filter(IS_QUALIFIER)
                        .toArray(Annotation[]::new);

                getBeanInstance(field, qualifiers)
                        .ifPresentOrElse(injected -> {
                            injectInstance(testInstance, field, injected);
                        }, () -> {
                            throw new RuntimeException("No injectable instance for " + field.toString());
                        });
            }
        }

    }

    private void injectInstance(Object testInstance, Field field, Object injected) {
        try {
            field.setAccessible(true);
            field.set(testInstance, injected);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<?> getBeanInstance(Field field, Annotation[] qualifiers) {
        Class<?> fieldType = field.getType();
        Optional<?> instanceOptional = CONTAINER.select(fieldType, qualifiers)
                .stream()
                .findAny();
        return instanceOptional;
    }

    private List<Field> getFields(Class<?> clazzInstance) {

        List<Field> fields = new ArrayList<>();
        if (!clazzInstance.getSuperclass().equals(Object.class)) {
            fields.addAll(getFields(clazzInstance.getSuperclass()));
        } else {
            fields.addAll(asList(clazzInstance.getDeclaredFields()));
        }

        return fields;

    }

}
