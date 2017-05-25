package com.bloggingit.odata.olingo.meta;

import com.bloggingit.odata.olingo.v4.util.DefaultValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.olingo.commons.api.data.ValueType;

/**
 * This class contains the meta data of a entity property for the OData service.
 */
@Getter
@AllArgsConstructor
@Builder
public class MetaEntityPropertyData {

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
    private final ValueType valueType;

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
        return ValueType.PRIMITIVE.equals(this.valueType);
    }
}
