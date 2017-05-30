package com.bloggingit.odata.olingo.v4.mapper;

import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyData;
import com.bloggingit.odata.olingo.v4.factory.OlingoObjectFactory;
import com.bloggingit.odata.olingo.v4.util.ReflectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;

/**
 *
 * @author mes
 */
public class OlingoPropertyMapper {

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> Object mapOlingoPropertyToObjPropertyValue(Property property, Class<?> fieldType) {

        Object val = null;
        if (property != null) {
            val = property.getValue();

            if (property.isEnum() && val != null) {
                val = EnumUtils.getEnumList((Class<E>) fieldType).get(Integer.parseInt(val.toString()));
            }
        }
        return val;
    }

            
    @SuppressWarnings("unchecked")
    public static <T, E extends Enum<E>> Property mapMetaPropertyDataToOlingoProperty(T objEntity, EntityMetaPropertyData metaProp) {

        Object val = ReflectionUtils.invokePropertyGetter(metaProp.getFieldName(), objEntity);

        if (metaProp.isEnum()) {
            val = EnumUtils.getEnum((Class<E>) metaProp.getFieldType(), String.valueOf(val)).ordinal();
        }

        ValueType valueType = OlingoEnumMapper.mapToValueType(metaProp.getValueType());
        Property property = OlingoObjectFactory.createProperty(metaProp.getName(), valueType, val);
        return property;
    }

}
