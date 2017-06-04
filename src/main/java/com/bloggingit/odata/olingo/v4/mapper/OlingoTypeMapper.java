package com.bloggingit.odata.olingo.v4.mapper;

import com.bloggingit.odata.olingo.edm.meta.EntityMetaProperty;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyComplex;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyEntity;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyEnum;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyPrimitve;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;

public class OlingoTypeMapper {

    /**
     * Maps classes to their corresponding ed, primitive {@code Class}es
     */
    public final static Map<List<Class<?>>, EdmPrimitiveTypeKind> CLASS_TO_TYPE_KIND_MAP = new HashMap<>();

    static {
        CLASS_TO_TYPE_KIND_MAP.put(Arrays.asList(String.class), EdmPrimitiveTypeKind.String);
        CLASS_TO_TYPE_KIND_MAP.put(Arrays.asList(boolean.class, Boolean.class), EdmPrimitiveTypeKind.Boolean);
        CLASS_TO_TYPE_KIND_MAP.put(Arrays.asList(byte.class, Byte.class), EdmPrimitiveTypeKind.SByte);
        CLASS_TO_TYPE_KIND_MAP.put(Arrays.asList(byte[].class, Byte[].class), EdmPrimitiveTypeKind.Binary);
        CLASS_TO_TYPE_KIND_MAP.put(Arrays.asList(short.class, Short.class), EdmPrimitiveTypeKind.Int16);
        CLASS_TO_TYPE_KIND_MAP.put(Arrays.asList(int.class, Integer.class), EdmPrimitiveTypeKind.Int32);
        CLASS_TO_TYPE_KIND_MAP.put(Arrays.asList(long.class, Long.class), EdmPrimitiveTypeKind.Int64);
        CLASS_TO_TYPE_KIND_MAP.put(Arrays.asList(float.class, Float.class), EdmPrimitiveTypeKind.Single);
        CLASS_TO_TYPE_KIND_MAP.put(Arrays.asList(double.class, Double.class), EdmPrimitiveTypeKind.Double);
        CLASS_TO_TYPE_KIND_MAP.put(Arrays.asList(BigInteger.class, BigDecimal.class), EdmPrimitiveTypeKind.Decimal);
        CLASS_TO_TYPE_KIND_MAP.put(Arrays.asList(Date.class), EdmPrimitiveTypeKind.Date);
        CLASS_TO_TYPE_KIND_MAP.put(Arrays.asList(Calendar.class), EdmPrimitiveTypeKind.DateTimeOffset);
        CLASS_TO_TYPE_KIND_MAP.put(Arrays.asList(UUID.class), EdmPrimitiveTypeKind.Guid);

    }

    public static ValueType mapToValueType(EntityMetaProperty metaProp) {
        ValueType valType = null;

        if (metaProp instanceof EntityMetaPropertyPrimitve) {
            valType = ValueType.PRIMITIVE;
        } else if (metaProp instanceof EntityMetaPropertyEnum) {
            valType = ValueType.ENUM;
        } else if (metaProp instanceof EntityMetaPropertyComplex) {
            valType = ValueType.COMPLEX;
        } else if (metaProp instanceof EntityMetaPropertyEntity) {
            valType = ValueType.ENTITY;
        }

        return valType;
    }

    public static EdmPrimitiveTypeKind mapToEdmPrimitiveTypeKind(Class<?> fieldType) {
        EdmPrimitiveTypeKind converted = null;

        for (Map.Entry<List<Class<?>>, EdmPrimitiveTypeKind> entry : CLASS_TO_TYPE_KIND_MAP.entrySet()) {
            if (entry.getKey().contains(fieldType)) {
                converted = entry.getValue();
                break;
            }
        }

        if (converted == null) {
            throw new UnsupportedOperationException("Not a supported type '" + fieldType + "'.");
        }

        return converted;
    }
}
