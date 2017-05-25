package com.bloggingit.odata.olingo.v4.util;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides the default value of any primitive type by creating an array of one
 * element and retrieving its first value.
 */
public class DefaultValue {

    /**
     * Map with primitive types
     */
    private static final Map<Class<?>, Object> DEFAULT_VALUES = Stream
            .of(boolean.class, byte.class, char.class, double.class, float.class, int.class, long.class, short.class)
            .collect(Collectors.toMap(clazz -> (Class<?>) clazz, clazz -> Array.get(Array.newInstance(clazz, 1), 0)));

    /**
     * Returns the default value for the given class.
     *
     * @param <T> the generic type of the class
     * @param clazz the class for which a default value is needed
     * @return A reasonable default value for the given class (the boxed default
     * value for primitives, <code>null</code> otherwise).
     */
    @SuppressWarnings("unchecked")
    public static <T> T forClass(Class<T> clazz) {
        return (T) DEFAULT_VALUES.get(clazz);
    }

}
