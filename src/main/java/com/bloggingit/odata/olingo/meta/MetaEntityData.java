package com.bloggingit.odata.olingo.meta;

import java.util.List;
import lombok.Getter;
import org.apache.olingo.commons.api.edm.FullQualifiedName;

/**
 *
 * This class contains the meta data of a entity for the OData service.
 *
 * @param <T> the generic type of the entity data
 */
@Getter
public class MetaEntityData<T> {

    /**
     * The suffix for a OData typeset
     */
    private static final String TYPE_SET_SUFFIX = "Set";

    /**
     * The corresponding entity class
     */
    private final Class<T> entityClass;

    /**
     * The name of the entity typeset.
     */
    private final String entityTypeSetName;

    /**
     * The name of the entity type.
     */
    private final String entityTypeName;

    /**
     * The full qualified name of the entity type.
     */
    private final FullQualifiedName entityTypeNameFQ;

    /**
     * Contains the list of all properties meta data.
     */
    private final List<MetaEntityPropertyData> properties;

    public MetaEntityData(Class<T> entityClass, String serviceNamespace, List<MetaEntityPropertyData> properties) {
        this.entityClass = entityClass;

        //generate values
        this.entityTypeName = this.entityClass.getSimpleName();
        this.entityTypeSetName = this.entityTypeName + TYPE_SET_SUFFIX;
        this.entityTypeNameFQ = new FullQualifiedName(serviceNamespace, this.entityClass.getSimpleName());

        this.properties = properties;
    }
}
