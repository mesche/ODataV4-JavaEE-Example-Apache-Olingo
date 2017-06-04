package com.bloggingit.odata.olingo.v4.mapper;

import com.bloggingit.odata.olingo.edm.meta.EntityMetaData;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaDataContainer;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaProperty;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyComplex;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyEntity;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyEnum;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyPrimitve;
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


    public static FullQualifiedName mapToPropertyValueTypeFQN(String serviceNamespace, EntityMetaProperty metaProp) {
        FullQualifiedName typeFQN = null;
        if (metaProp instanceof EntityMetaPropertyEnum || metaProp instanceof EntityMetaPropertyComplex || metaProp instanceof EntityMetaPropertyEntity) {
            typeFQN = createFullQualifiedName(serviceNamespace, metaProp.getFieldType().getSimpleName());
        } else if (metaProp instanceof EntityMetaPropertyPrimitve) {
            typeFQN = OlingoTypeMapper.mapToEdmPrimitiveTypeKind(metaProp.getFieldType()).getFullQualifiedName();
        }
        return typeFQN;
    }

}
