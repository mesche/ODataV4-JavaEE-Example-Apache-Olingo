package com.bloggingit.odata.olingo.v4.provider;

import com.bloggingit.odata.olingo.v4.mapper.OlingoObjectMapper;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaData;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaDataContainer;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaDataBuilder;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaProperty;
import com.bloggingit.odata.olingo.v4.factory.OlingoObjectFactory;
import com.bloggingit.odata.olingo.v4.mapper.FQNMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlStructuralType;
import org.apache.olingo.commons.api.ex.ODataException;

/**
 * Provider-implementation of a {@link CsdlAbstractEdmProvider} for annotation
 * support in the entity data model.
 */
public class AnnotationEdmProvider extends CsdlAbstractEdmProvider {

    @Getter
    private final EntityMetaDataContainer entityMetaDataContainer;

    public AnnotationEdmProvider(String serviceNamespace, String edmContainer, String dtoPackage) {
        this.entityMetaDataContainer = EntityMetaDataBuilder.createContainer(serviceNamespace, edmContainer, dtoPackage);
    }

    @Override
    public List<CsdlSchema> getSchemas() {
        List<CsdlEntityType> entityTypes = new ArrayList<>();
        List<CsdlEnumType> enumTypes = new ArrayList<>();
        List<CsdlComplexType> complexTypes = new ArrayList<>();

        String serviceNamespace = this.entityMetaDataContainer.getServiceNamespace();

        this.entityMetaDataContainer.getAllEntityMetaData().forEach((meta) -> {
            CsdlStructuralType structuralType = OlingoObjectMapper.mapEntityMetaDataToCsdlStructuralType(meta, serviceNamespace);

            if (structuralType instanceof CsdlComplexType) {
                complexTypes.add((CsdlComplexType) structuralType);
            } else {
                entityTypes.add((CsdlEntityType) structuralType);
            }

            enumTypes.addAll(OlingoObjectMapper.mapEntityMetaDataToCsdlEnumTypeList(meta));
        });

        CsdlSchema schema = OlingoObjectFactory
                .createCsdlSchema(serviceNamespace, getEntityContainer(), entityTypes, enumTypes, complexTypes);

        return Arrays.asList(schema);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CsdlEnumType getEnumType(FullQualifiedName enumTypeNameFQ) throws ODataException {
        CsdlEnumType csdlEnumType = null;
        if (enumTypeNameFQ != null
                && this.entityMetaDataContainer.getServiceNamespace().equals(enumTypeNameFQ.getNamespace())) {

            EntityMetaProperty propertyData = this.entityMetaDataContainer.getEntityMetaPropertyDataByTypeName(enumTypeNameFQ.getNamespace(), enumTypeNameFQ.getName());

            if (propertyData != null) {
                Class<Enum> enumFieldType = (Class<Enum>) propertyData.getFieldType();
                csdlEnumType = OlingoObjectFactory.createEnumType(enumFieldType);
            }
        }

        return csdlEnumType;
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeNameFQ) {
        EntityMetaData<?> metaData = this.entityMetaDataContainer.getEntityMetaDataByTypeName(entityTypeNameFQ.getNamespace(), entityTypeNameFQ.getName());

        return (CsdlEntityType) OlingoObjectMapper.mapEntityMetaDataToCsdlStructuralType(metaData, this.entityMetaDataContainer.getServiceNamespace());
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
    public CsdlComplexType getComplexType(FullQualifiedName complexTypeNameFQ) throws ODataException {
        EntityMetaData<?> metaData = this.entityMetaDataContainer.getEntityMetaDataByTypeName(complexTypeNameFQ.getNamespace(), complexTypeNameFQ.getName());

        if (metaData.isComplexType()) {
            return (CsdlComplexType) OlingoObjectMapper.mapEntityMetaDataToCsdlStructuralType(metaData, this.entityMetaDataContainer.getServiceNamespace());
        } else {
            return null;
        }
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
