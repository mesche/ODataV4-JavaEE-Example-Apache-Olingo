package com.bloggingit.odata.olingo.v4.mapper;

import com.bloggingit.odata.olingo.edm.meta.EntityMetaData;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaDataContainer;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyData;
import org.apache.olingo.commons.api.edm.FullQualifiedName;

/**
 *
 * @author mes
 */
public class FQNMapper {

    public static FullQualifiedName createFullQualifiedName(String serviceNamespace, String name) {
        return new FullQualifiedName(serviceNamespace, name);
    }

    public static FullQualifiedName createFullQualifiedName(EntityMetaDataContainer entityMetaDataContainer) {
        return createFullQualifiedName(entityMetaDataContainer.getServiceNamespace(), entityMetaDataContainer.getEdmContainerName());
    }

    public static <T> FullQualifiedName createFullQualifiedName(String serviceNamespace, EntityMetaData<T> meta) {
        return createFullQualifiedName(serviceNamespace, meta.getEntityTypeName());
    }


    public static FullQualifiedName mapToValueTypeFQN(String serviceNamespace, EntityMetaPropertyData metaProp) {
        FullQualifiedName typeFQN;
        if (metaProp.isEnum()) {
            typeFQN = createFullQualifiedName(serviceNamespace, metaProp.getFieldType().getSimpleName());
        } else {
            typeFQN = OlingoEnumMapper.mapToEdmPrimTypeKind(metaProp.getFieldType()).getFullQualifiedName();
        }
        return typeFQN;
    }

}
