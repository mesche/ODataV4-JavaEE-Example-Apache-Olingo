package com.bloggingit.odata.olingo.edm.meta;

import lombok.Builder;
import lombok.Getter;

/**
 * This class contains the meta data of a primitive property for the OData
 * service.
 */
@Getter
public class EntityMetaPropertyPrimitve extends EntityMetaProperty {

    /**
     * The maximum length of the type in use as Integer.
     *
     * A negative value indicates for the EDM provider an unset/default value.
     */
    private final int maxLength;

    /**
     * The precision of the type in use as Integer.
     *
     * A negative value indicates for the EDM provider an unset/default value.
     */
    private final int precision;

    /**
     * The scale of the type in use as Integer.
     *
     * A negative value indicates for the EDM provider an unset/default value.
     */
    private final int scale;

    /**
     * true if unicode or null if not specified
     */
    private final Boolean unicode;

    @Builder
    private EntityMetaPropertyPrimitve(
            boolean isKey,
            String name,
            String fieldName,
            Class<?> fieldType,
            Boolean nullable,
            int maxLength,
            int precision,
            int scale,
            Boolean unicode) {

        super(isKey, name, fieldName, fieldType, nullable);
        this.maxLength = maxLength;
        this.precision = precision;
        this.scale = scale;
        this.unicode = unicode;
    }
}
