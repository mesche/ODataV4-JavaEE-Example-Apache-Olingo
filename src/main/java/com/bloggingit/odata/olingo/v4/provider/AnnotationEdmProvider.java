package com.bloggingit.odata.olingo.v4.provider;

import com.bloggingit.odata.olingo.annotation.edm.EdmEntityType;
import com.bloggingit.odata.olingo.annotation.edm.EdmKey;
import com.bloggingit.odata.olingo.annotation.edm.EdmProperty;
import com.bloggingit.odata.olingo.mapper.OlingoEntityMapper;
import com.bloggingit.odata.olingo.meta.MetaEntityData;
import com.bloggingit.odata.olingo.meta.MetaEntityDataCollection;
import com.bloggingit.odata.olingo.meta.MetaEntityPropertyData;
import com.bloggingit.odata.olingo.v4.util.ReflectionUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.olingo.commons.api.data.ValueType;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * Provider-implementation of a {@link CsdlAbstractEdmProvider} for annotation
 * support in the entity data model.
 */
public class AnnotationEdmProvider extends CsdlAbstractEdmProvider {

    @Getter
    private final MetaEntityDataCollection metaEntityDataCollection;
    private final FullQualifiedName edmContainerFQ;

    private final String serviceNamespace;

    public AnnotationEdmProvider(String serviceNamespace, String edmContainer, String dtoPackage) {
        this.serviceNamespace = serviceNamespace;

        ConcurrentMap<String, MetaEntityData<?>> metaEntities = createAllMetaEntityData(dtoPackage, serviceNamespace);

        this.metaEntityDataCollection = new MetaEntityDataCollection(metaEntities);

        this.edmContainerFQ = new FullQualifiedName(serviceNamespace, edmContainer);
    }

    private Set<Class<?>> findODataEntityClasses(String dtoPackage) {
        return ReflectionUtils.findClassesInPackageAnnotatedWith(dtoPackage, EdmEntityType.class);
    }

    @Override
    public List<CsdlSchema> getSchemas() {

        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(this.serviceNamespace);

        this.metaEntityDataCollection.getAllMetaEntityData().values().stream().map((meta) -> {
            List<CsdlEntityType> entityTypes = new ArrayList<>();
            entityTypes.add(getEntityType(meta.getEntityTypeNameFQ()));
            return entityTypes;
        }).map((entityTypes) -> {
            schema.setEntityTypes(entityTypes);
            return entityTypes;
        }).forEachOrdered((_item) -> {
            schema.setEntityContainer(getEntityContainer());
        });

        List<CsdlSchema> schemas = new ArrayList<>();
        schemas.add(schema);

        return schemas;
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeNameFQ) {

        MetaEntityData<?> metaData = this.metaEntityDataCollection.getMetaEntityDataByTypeNameFQ(entityTypeNameFQ);

        CsdlEntityType entityType = OlingoEntityMapper.mapMetaEntityDataToCsdlEntityType(metaData);

        return entityType;
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainerFQ, String entitySetName) {
        CsdlEntitySet entitySet = null;
        if (entityContainerFQ.equals(this.edmContainerFQ)) {
            MetaEntityData<?> meta = this.metaEntityDataCollection.getMetaEntityDataByTypeSetName(entitySetName);
            if (meta != null) {
                entitySet = new CsdlEntitySet();
                entitySet.setName(meta.getEntityTypeSetName());
                entitySet.setType(meta.getEntityTypeNameFQ());
            }
        }

        return entitySet;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() {
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(this.edmContainerFQ.getName());

        List<CsdlEntitySet> entitySets = new ArrayList<>();

        this.metaEntityDataCollection.getAllMetaEntityData().values().forEach((metaData) -> {
            entitySets.add(getEntitySet(this.edmContainerFQ, metaData.getEntityTypeSetName()));
        });

        entityContainer.setEntitySets(entitySets);

        return entityContainer;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {
        if (entityContainerName == null || entityContainerName.equals(this.edmContainerFQ)) {
            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(this.edmContainerFQ);
            return entityContainerInfo;
        }

        return null;
    }

    private <T> ConcurrentMap<String, MetaEntityData<?>> createAllMetaEntityData(String dtoPackage, String serviceNamespace) {
        ConcurrentMap<String, MetaEntityData<?>> metaEntities = new ConcurrentHashMap<>();

        Set<Class<?>> odataEntityClasses = findODataEntityClasses(dtoPackage);

        odataEntityClasses.stream().map((entityClass) -> {
            List<MetaEntityPropertyData> properties = createEntityProperties(entityClass);
            MetaEntityData<?> entity = new MetaEntityData<>(entityClass, serviceNamespace, properties);
            return entity;
        }).forEachOrdered((entity) -> {
            metaEntities.put(entity.getEntityTypeSetName(), entity);
        });

        return metaEntities;
    }

    private List<MetaEntityPropertyData> createEntityProperties(Class<?> entityClass) {
        List<MetaEntityPropertyData> newProperties = new ArrayList<>();

        Field[] fields = FieldUtils.getFieldsWithAnnotation(entityClass, EdmProperty.class);

        for (Field field : fields) {

            EdmProperty edmProperty = field.getAnnotation(EdmProperty.class);

            boolean isKey = (field.isAnnotationPresent(EdmKey.class));

            String name = (StringUtils.isNotBlank(edmProperty.name())) ? edmProperty.name() : field.getName();

            MetaEntityPropertyData propertyData = MetaEntityPropertyData
                    .builder()
                    .name(name)
                    .fieldName(field.getName())
                    .fieldType(field.getType())
                    .isKey(isKey)
                    .maxLength(edmProperty.facets().maxLength())
                    .nullable(edmProperty.facets().nullable())
                    .precision(edmProperty.facets().precision())
                    .scale(edmProperty.facets().scale())
                    .valueType(ValueType.PRIMITIVE)
                    .build();

            newProperties.add(propertyData);
        }

        return newProperties;
    }
}
