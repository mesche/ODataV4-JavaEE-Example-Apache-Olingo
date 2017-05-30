package com.bloggingit.odata.olingo.v4.mapper;

import com.bloggingit.odata.edm.enumeration.EdmValueType;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;

/**
 *
 * @author mes
 */
public class OlingoEnumMapper {
    /**
     * Maps names of primitives to their corresponding primitive {@code Class}es
     */
    public final static Map<Class<?>, Class<?>> PRIMITIVE_NAME_MAP = new HashMap<Class<?>, Class<?>>();

    static {
        PRIMITIVE_NAME_MAP.put(boolean.class, Boolean.class);
        PRIMITIVE_NAME_MAP.put(byte.class, Byte.class);
        PRIMITIVE_NAME_MAP.put(short.class, Short.class);
        PRIMITIVE_NAME_MAP.put(char.class, Character.class);
        PRIMITIVE_NAME_MAP.put(int.class, Integer.class);
        PRIMITIVE_NAME_MAP.put(long.class, Long.class);
        PRIMITIVE_NAME_MAP.put(float.class, Float.class);
        PRIMITIVE_NAME_MAP.put(double.class, Double.class);
    }

    public static ValueType mapToValueType(EdmValueType edmValueType) {
        return ValueType.valueOf(edmValueType.name());
    }

    public static EdmPrimitiveTypeKind mapToEdmPrimTypeKind(Class<?> fieldType) {
        EdmPrimitiveTypeKind converted;

        if (fieldType.isAssignableFrom(Long.TYPE)) {
            converted = EdmPrimitiveTypeKind.Int64;
        } else if (fieldType.isAssignableFrom(Date.class)) {
            converted = EdmPrimitiveTypeKind.DateTimeOffset;
        } else {
            String typeName;
            if (fieldType.isPrimitive() && PRIMITIVE_NAME_MAP.containsKey(fieldType)) {
                typeName = PRIMITIVE_NAME_MAP.get(fieldType).getSimpleName();
            } else {
                typeName = fieldType.getSimpleName();
            }

            converted = EdmPrimitiveTypeKind.valueOf(typeName);
        }

        return converted;
    }
}
