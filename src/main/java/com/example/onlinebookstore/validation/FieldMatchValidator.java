package com.example.onlinebookstore.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import org.apache.commons.beanutils.PropertyUtils;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {
    private String first;
    private String second;

    @Override
    public void initialize(FieldMatch constraintAnnotation) {
        this.first = constraintAnnotation.first();
        this.second = constraintAnnotation.second();
    }

    @Override
    public boolean isValid(Object bean, ConstraintValidatorContext constraintValidatorContext) {
        try {
            Object firstObj = PropertyUtils.getProperty(bean, this.first);
            Object secondObj = PropertyUtils.getProperty(bean, this.second);
            return Objects.equals(firstObj, secondObj);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Couldn't get value from specified bean" + bean, e);
        }
    }
}
