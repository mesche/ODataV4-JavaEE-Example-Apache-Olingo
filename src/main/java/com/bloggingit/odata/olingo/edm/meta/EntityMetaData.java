package com.bloggingit.odata.olingo.edm.meta;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 *
 * This class contains the meta data of a entity for the OData service.
 *
 * @param <T> the generic type of the entity data
 */
@Getter
public class EntityMetaData<T> {

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

    private final boolean isEntitySet;

    /**
     * Contains the list of all properties meta data.
     */
    private final List<EntityMetaPropertyData> properties;

    public EntityMetaData(Class<T> entityClass, String serviceNamespace, boolean isEntitySet, List<EntityMetaPropertyData> properties) {
        this.entityClass = entityClass;

        //generate values
        this.entityTypeName = this.entityClass.getSimpleName();
        this.isEntitySet = isEntitySet;

        this.entityTypeSetName = (isEntitySet) ? this.entityTypeName + TYPE_SET_SUFFIX : null;

        this.properties = properties;
    }

    public List<EntityMetaPropertyData> getEnumPropertyData() {
        List<EntityMetaPropertyData> enumPropertyList = new ArrayList<>();

        this.properties.stream().filter((prop) -> (prop.isEnum())).forEachOrdered((prop) -> {
            enumPropertyList.add(prop);
        });
        return enumPropertyList;
    }
}
