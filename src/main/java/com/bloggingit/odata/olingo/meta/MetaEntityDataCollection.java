package com.bloggingit.odata.olingo.meta;

import java.util.concurrent.ConcurrentMap;
import lombok.Getter;
import org.apache.olingo.commons.api.edm.FullQualifiedName;

/**
 *
 * This class holds all meta data of multiple entities.
 */
public class MetaEntityDataCollection {

    /**
     * List of meta data for multiple entities. The key is the name of the
     * entity's typeset.
     */
    @Getter
    private final ConcurrentMap<String, MetaEntityData<?>> allMetaEntityData;

    public MetaEntityDataCollection(ConcurrentMap<String, MetaEntityData<?>> allMetaEntityData) {
        this.allMetaEntityData = allMetaEntityData;
    }


    public MetaEntityData<?> getMetaEntityDataByTypeSetName(String entityTypeSetName) {
        return this.allMetaEntityData.get(entityTypeSetName);
    }

    public MetaEntityData<?> getMetaEntityDataByTypeNameFQ(FullQualifiedName entityTypeNameFQ) {
        MetaEntityData<?> entity = null;
        for (MetaEntityData<?> meta : this.allMetaEntityData.values()) {
            if (meta.getEntityTypeNameFQ().equals(entityTypeNameFQ)) {
                entity = meta;
                break;
            }
        }
        return entity;
    }
}
