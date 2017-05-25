package com.bloggingit.odata.olingo.annotation.edm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for definition of an EdmProperty as EdmKey for the EdmEntityType
 * which contains the EdmProperty.
 * <p>
 * This annotation can not be parameterized, all values like name are defined
 * via the {@link EdmProperty} annotation. In addition the EdmKey annotation has
 * to be used in conjunction with an EdmProperty annotation on a field within a
 * EdmEntityType annotated class.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EdmKey {
}
