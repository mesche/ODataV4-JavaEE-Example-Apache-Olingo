package com.bloggingit.odata.olingo.v4.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.reflections.Reflections;

/**
 * Simple utility class with convenient helper methods for simple working with
 * the java reflection API.
 */
public class ReflectionUtils {

    /**
     * Get classes annotated with a given annotation in a certain package.
     *
     * @param dtoPackage search in this certain package
     * @param annotationClass find classes annotated with this annotation
     * @return the classes with the given annotation
     */
    public static Set<Class<?>> findClassesInPackageAnnotatedWith(String dtoPackage, Class<? extends Annotation> annotationClass) {
        Reflections reflections = new Reflections(dtoPackage);

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(annotationClass);

        return classes;

    }

    /**
     * Invoke the getter method of the given property for the specified object
     *
     * @param propertyName invoke the getter of this property
     * @param entity invoke the getter for this object
     * @return the result of the getter method
     */
    public static Object invokePropertyGetter(String propertyName, Object entity) {
        try {
            return new PropertyDescriptor(propertyName, entity.getClass()).getReadMethod().invoke(entity); //invoke getter 
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new ODataRuntimeException(ex);
        }
    }

    /**
     * Invoke the setter method of the given property for the specified object
     * and sets the given value
     *
     * @param propertyName invoke the setter of this property
     * @param entity invoke the setter for this object
     * @param val the new value for this property
     */
    public static void invokePropertySetter(String propertyName, Object entity, Object val) {
        try {
            Method setMethod = new PropertyDescriptor(propertyName, entity.getClass()).getWriteMethod(); // get setter
            setMethod.invoke(entity, val); //invoke setter 
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NullPointerException ex) {
            throw new ODataRuntimeException(ex);
        }
    }
}
