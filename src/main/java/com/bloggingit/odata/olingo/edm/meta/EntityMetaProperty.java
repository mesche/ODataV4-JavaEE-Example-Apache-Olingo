package com.bloggingit.odata.olingo.edm.meta;

import com.bloggingit.odata.olingo.v4.util.DefaultValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This abstract class is for the meta data of an entity property
 */
@Getter
@AllArgsConstructor
public abstract class EntityMetaProperty {

    /**
     * true if property is annotated with @EdmKey or false if not
     */
    private final boolean isKey;

    /**
     * The name of the edm property.
     */
    private final String edmName;

    /**
     * The property name of the entity.
     */
    private final String fieldName;

    /**
     * The property type of the entity.
     */
    private final Class<?> fieldType;

    /**
     * true if nullable or null if not specified
     */
    private final Boolean nullable;

    /**
     * @return the default value as a String or null if not specified
     */
    public Object getDefaultValue() {
        return DefaultValue.forClass(this.getFieldType());
    }
}
