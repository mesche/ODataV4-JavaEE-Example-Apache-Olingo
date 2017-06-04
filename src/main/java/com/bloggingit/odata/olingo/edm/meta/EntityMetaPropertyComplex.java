package com.bloggingit.odata.olingo.edm.meta;

import lombok.Builder;
import lombok.Getter;

/**
 * This class contains the meta data of a complex property for the OData
 * service.
 */
@Getter
public class EntityMetaPropertyComplex extends EntityMetaProperty {

    /**
     * The type of the property value.
     */
    private final EntityMetaData<?> valueMetaData;

    @Builder
    private EntityMetaPropertyComplex(
            String name,
            String fieldName,
            Class<?> fieldType,
            Boolean nullable,
            EntityMetaData<?> valueMetaData) {

        super(false, name, fieldName, fieldType, nullable);
        this.valueMetaData = valueMetaData;
    }
}
