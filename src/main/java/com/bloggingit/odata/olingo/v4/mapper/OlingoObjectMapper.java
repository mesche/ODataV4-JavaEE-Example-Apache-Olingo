package com.bloggingit.odata.olingo.v4.mapper;

import com.bloggingit.odata.olingo.edm.meta.EntityMetaData;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyData;
import com.bloggingit.odata.olingo.v4.factory.OlingoObjectFactory;
import com.bloggingit.odata.olingo.v4.util.ReflectionUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;

/**
 * Mapper provides methods to convert data into a certain Apache Olingo data
 * structure.
 */
public class OlingoObjectMapper {

    public static <T> EntityCollection mapObjectEntitiesToOlingoEntityCollection(Collection<T> objEntities, EntityMetaData<T> entityMetaData) {
        EntityCollection entityCollection = new EntityCollection();

        objEntities.forEach((entity) -> {
            entityCollection
                    .getEntities()
                    .add(mapObjEntityToOlingoEntity(entity, entityMetaData));
        });
        return entityCollection;
    }

    public static <T> Entity mapObjEntityToOlingoEntity(T objEntity, EntityMetaData<T> entityMetaData) {
        final Entity entity = new Entity();

        List<EntityMetaPropertyData> metaProperties = entityMetaData.getProperties();

        metaProperties.forEach((metaProp) -> {
            Property property = OlingoPropertyMapper.mapMetaPropertyDataToOlingoProperty(objEntity, metaProp);
            entity.addProperty(property);
            if (metaProp.isKey()) {
                entity.setId(OlingoObjectFactory.createId(entityMetaData.getEntityTypeSetName(), property.getValue()));
            }
        });

        return entity;
    }

    public static <T> T mapOlingoEntityToObjectEntity(EntityMetaData<T> entityMetaData, Entity entity) {

        T objEntity = OlingoObjectFactory.createInstance(entityMetaData.getEntityClass());

        List<EntityMetaPropertyData> metaProperties = entityMetaData.getProperties();

        metaProperties.forEach((metaProp) -> {
            Property property = entity.getProperty(metaProp.getName());
            Object val = OlingoPropertyMapper.mapOlingoPropertyToObjPropertyValue(property, metaProp.getFieldType());
            ReflectionUtils.invokePropertySetter(metaProp.getFieldName(), objEntity, val);
        });


        return objEntity;
    }

    public static <T> CsdlEntityType mapEntityMetaDataToCsdlEntityType(EntityMetaData<T> metaData, String serviceNamespace) {
        Class<T> objEntityClass = metaData.getEntityClass();

        CsdlEntityType entityType = null;

        List<CsdlProperty> properties = new ArrayList<>();
        List<CsdlPropertyRef> keys = new ArrayList<>();

        for (EntityMetaPropertyData metaProp : metaData.getProperties()) {
            FullQualifiedName typeFQN = FQNMapper.mapToValueTypeFQN(serviceNamespace, metaProp);

            Object objEntity = OlingoObjectFactory.createInstance(objEntityClass);
            Object val = ReflectionUtils.invokePropertyGetter(metaProp.getFieldName(), objEntity);

            properties.add(OlingoObjectFactory.createCsdlProperty(metaProp, typeFQN, String.valueOf(val)));

            if (metaProp.isKey()) {
                keys.add(OlingoObjectFactory.createCsdlPropertyRef(metaProp.getName()));
            }

            entityType = OlingoObjectFactory.createCsdlEntityType(metaData, properties, keys);
        }

        return entityType;
    }
}
