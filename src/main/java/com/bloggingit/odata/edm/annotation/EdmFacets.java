package com.bloggingit.odata.edm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation for definition of EdmFactes on an EdmProperty (for an
 * EdmEntityType or EdmComplexType which contains the EdmProperty as a
 * field).
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface EdmFacets {

    /**
     * The maximum length of the type in use. A negative value indicates for the
     * EDM provider an unset/default value.
     *
     * @return the maximum length of the type in use as Integer
     */
    int maxLength() default -1;

    /**
     * The scale of the type in use. A negative value indicates for the EDM
     * provider an unset/default value.
     *
     * @return the scale of the type in use as Integer
     */
    int scale() default -1;

    /**
     * The precision of the type in use. A negative value indicates for the EDM
     * provider an unset/default value.
     *
     * @return the precision of the type in use as Integer
     */
    int precision() default -1;

    /**
     * The information if the type in use is nullable. The default value for
     * nullable is <code>true</code>.
     *
     * @return <code>true</code> if the type in use is nullable,
     * <code>false</code> otherwise.
     */
    boolean nullable() default true;
}
