package com.bloggingit.odata.olingo.edm.meta;

import com.bloggingit.odata.edm.enumeration.EdmValueType;
import com.bloggingit.odata.olingo.v4.util.DefaultValue;
import lombok.Builder;
import lombok.Getter;

/**
 * This class contains the meta data of a entity property for the OData service.
 */
@Getter
@Builder
public class EntityMetaPropertyData {

    /**
     * The name of the OData property.
     */
    private final String name;

    /**
     * The property name of the entity.
     */
    private final String fieldName;

    /**
     * The property type of the entity.
     */
    private final Class<?> fieldType;

    /**
     * true if property is annotated with @EdmKey or false if not
     */
    private final boolean isKey;

    /**
     * The type of the property value.
     */
    private final EdmValueType valueType;


    /**
     * The maximum length of the type in use as Integer.
     *
     * A negative value indicates for the EDM provider an unset/default value.
     */
    private final int maxLength;

    /**
     * The scale of the type in use as Integer.
     *
     * A negative value indicates for the EDM provider an unset/default value.
     */
    private final int scale;

    /**
     * The precision of the type in use as Integer.
     *
     * A negative value indicates for the EDM provider an unset/default value.
     */
    private final int precision;

    /**
     * true if nullable or null if not specified
     */
    private final Boolean nullable;

    /**
     * true if unicode or null if not specified
     */
    private final Boolean unicode;

    /**
     * @return the default value as a String or null if not specified
     */
    public Object getDefaultValue() {
        return (this.isPrimitive()) ? DefaultValue.forClass(this.getFieldType()) : null;
    }

    /**
     * Gets the info if the property is a primitive property.
     *
     * @return true, if it is a primitive property
     */
    public boolean isPrimitive() {
        return EdmValueType.PRIMITIVE.equals(this.valueType);
    }

    /**
     * Gets the info if the property is a enum property.
     *
     * @return true, if it is a enum property
     */
    public boolean isEnum() {
        return EdmValueType.ENUM.equals(this.valueType);
    }

    /**
     * Gets the info if the property is a complex property.
     *
     * @return true, if it is a complex property
     */
    public boolean isComplex() {
        return EdmValueType.COMPLEX.equals(this.valueType);
    }
}
