package com.bloggingit.odata.olingo.v4.provider;

import com.bloggingit.odata.olingo.v4.mapper.OlingoObjectMapper;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaData;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaDataContainer;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaDataFactory;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyData;
import com.bloggingit.odata.olingo.v4.factory.OlingoObjectFactory;
import com.bloggingit.odata.olingo.v4.mapper.FQNMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.Getter;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;

/**
 * Provider-implementation of a {@link CsdlAbstractEdmProvider} for annotation
 * support in the entity data model.
 */
public class AnnotationEdmProvider extends CsdlAbstractEdmProvider {

    @Getter
    private final EntityMetaDataContainer entityMetaDataContainer;

    public AnnotationEdmProvider(String serviceNamespace, String edmContainer, String dtoPackage) {

        Set<EntityMetaData<?>> metaEntities = EntityMetaDataFactory
                .createEntityMetaDataList(dtoPackage, serviceNamespace);

        this.entityMetaDataContainer = new EntityMetaDataContainer(serviceNamespace, edmContainer, metaEntities);
    }

    @Override
    public List<CsdlSchema> getSchemas() {
        List<CsdlEntityType> entityTypes = new ArrayList<>();
        List<CsdlEnumType> enumTypes = new ArrayList<>();

        this.entityMetaDataContainer.getAllEntityMetaData().forEach((meta) -> {
            CsdlEntityType csdlEntityType = OlingoObjectMapper.mapEntityMetaDataToCsdlEntityType(meta, this.entityMetaDataContainer.getServiceNamespace());

            entityTypes.add(csdlEntityType);

            enumTypes.addAll(OlingoObjectFactory.createEnumTypeList(meta));
        });

        CsdlSchema schema = OlingoObjectFactory.createCsdlSchema(this.entityMetaDataContainer.getServiceNamespace(), getEntityContainer(), entityTypes, enumTypes);

        return Arrays.asList(schema);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CsdlEnumType getEnumType(FullQualifiedName enumTypeNameFQ) throws ODataException {
        CsdlEnumType csdlEnumType = null;
        if (enumTypeNameFQ != null
                && this.entityMetaDataContainer.getServiceNamespace().equals(enumTypeNameFQ.getNamespace())) {

            EntityMetaPropertyData propertyData = this.entityMetaDataContainer.getEntityMetaPropertyDataByTypeName(enumTypeNameFQ.getNamespace(), enumTypeNameFQ.getName());

            Class<Enum> enumFieldType = (Class<Enum>) propertyData.getFieldType();

            csdlEnumType = OlingoObjectFactory.createEnumType(enumFieldType);
        }

        return csdlEnumType;
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeNameFQ) {
        EntityMetaData<?> metaData = this.entityMetaDataContainer.getEntityMetaDataByTypeName(entityTypeNameFQ.getNamespace(), entityTypeNameFQ.getName());

        CsdlEntityType entityType = OlingoObjectMapper.mapEntityMetaDataToCsdlEntityType(metaData, this.entityMetaDataContainer.getServiceNamespace());

        return entityType;
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainerFQ, String entitySetName) {
        CsdlEntitySet entitySet = null;

        if (entityContainerFQ != null
                && this.entityMetaDataContainer.getServiceNamespace().equals(entityContainerFQ.getNamespace())
                && this.entityMetaDataContainer.getEdmContainerName().equals(entityContainerFQ.getName())) {

            EntityMetaData<?> meta = this.entityMetaDataContainer.getEntityMetaDataByTypeSetName(entitySetName);
            if (meta != null) {
                entitySet = OlingoObjectFactory.createCsdlEntitySet(meta, entityContainerFQ.getNamespace());
            }
        }

        return entitySet;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() {
        List<CsdlEntitySet> entitySets = new ArrayList<>();

        this.entityMetaDataContainer.getAllEntityMetaData().forEach((metaData) -> {
            FullQualifiedName containerNameFQ = FQNMapper.createFullQualifiedName(this.entityMetaDataContainer);

            CsdlEntitySet entitySet = getEntitySet(containerNameFQ, metaData.getEntityTypeSetName());
            if (entitySet != null) {
                entitySets.add(entitySet);
            }
        });

        return OlingoObjectFactory.createCsdlEntityContainer(this.entityMetaDataContainer.getEdmContainerName(), entitySets);
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerFQ) {
        String serviceNamespace;
        String edmContainerName;

        if (entityContainerFQ == null) {
            serviceNamespace = this.entityMetaDataContainer.getServiceNamespace();
            edmContainerName = this.entityMetaDataContainer.getEdmContainerName();
        } else {
            serviceNamespace = entityContainerFQ.getNamespace();
            edmContainerName = entityContainerFQ.getName();
        }

        return OlingoObjectFactory.createCsdlEntityContainerInfo(serviceNamespace, edmContainerName);
    }

}
