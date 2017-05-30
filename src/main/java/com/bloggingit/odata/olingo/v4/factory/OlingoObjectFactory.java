package com.bloggingit.odata.olingo.v4.factory;

import com.bloggingit.odata.olingo.edm.meta.EntityMetaData;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyData;
import com.bloggingit.odata.olingo.v4.mapper.FQNMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.EnumUtils;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;

/**
 *
 * @author mes
 */
public class OlingoObjectFactory {

    public static <T> CsdlEntitySet createCsdlEntitySet(EntityMetaData<T> meta, String serviceNamespace) {
        CsdlEntitySet csdlEntitySet = new CsdlEntitySet();

        csdlEntitySet.setName(meta.getEntityTypeSetName());

        csdlEntitySet.setType(FQNMapper.createFullQualifiedName(serviceNamespace, meta));

        return csdlEntitySet;
    }

    public static CsdlEntityContainer createCsdlEntityContainer(String containerName, List<CsdlEntitySet> entitySets) {
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();

        entityContainer.setName(containerName);

        entityContainer.setEntitySets(entitySets);

        return entityContainer;
    }

    public static CsdlEntityContainerInfo createCsdlEntityContainerInfo(String serviceNamespace, String containerName) {
        CsdlEntityContainerInfo csdlEntityContainerInfo = new CsdlEntityContainerInfo();

        FullQualifiedName containerNameFQ = FQNMapper.createFullQualifiedName(serviceNamespace, containerName);

        csdlEntityContainerInfo.setContainerName(containerNameFQ);

        return csdlEntityContainerInfo;
    }

    public static CsdlSchema createCsdlSchema(String serviceNamespace, CsdlEntityContainer entityContainer, List<CsdlEntityType> entityTypes, List<CsdlEnumType> enumTypes) {
        CsdlSchema csdlSchema = new CsdlSchema();

        csdlSchema.setNamespace(serviceNamespace);
        csdlSchema.setEntityContainer(entityContainer);
        csdlSchema.setEntityTypes(entityTypes);
        csdlSchema.setEnumTypes(enumTypes);

        return csdlSchema;
    }

    public static CsdlProperty createCsdlProperty(EntityMetaPropertyData metaProp, FullQualifiedName typeFQN, String defaultVal) {
        CsdlProperty csdlProperty = new CsdlProperty();

        csdlProperty.setName(metaProp.getName()).setType(typeFQN);

        if (metaProp.isPrimitive()) {
            csdlProperty
                    .setNullable(metaProp.getNullable());

            if (metaProp.getMaxLength() != -1) {
                csdlProperty.setMaxLength(metaProp.getMaxLength());
            }
            if (metaProp.getPrecision() != -1) {
                csdlProperty.setPrecision(metaProp.getPrecision());
            }
            if (metaProp.getScale() != -1) {
                csdlProperty.setScale(metaProp.getScale());
            }
            if (defaultVal != null) {
                csdlProperty.setDefaultValue(defaultVal);
            }
        }

        return csdlProperty;
    }

    public static <E extends Enum<E>> CsdlEnumType createEnumType(final Class<E> enumClass) {

        List<E> enumList = EnumUtils.getEnumList(enumClass);

        List<CsdlEnumMember> csdlEnumMember = new ArrayList<>();

        enumList.forEach((entry) -> {
            csdlEnumMember.add(new CsdlEnumMember().setName(entry.name()).setValue(String.valueOf(entry.ordinal())));
        });

        return new CsdlEnumType()
                .setName(enumClass.getSimpleName())
                .setMembers(csdlEnumMember)
                .setUnderlyingType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends Enum<E>> List<CsdlEnumType> createEnumTypeList(final EntityMetaData<T> meta) {
        List<CsdlEnumType> enumTypes = new ArrayList<>();

        meta.getEnumPropertyData().forEach((prop) -> {
            enumTypes.add(createEnumType((Class<E>) prop.getFieldType()));
        });

        return enumTypes;

    }

    public static <T> CsdlEntityType createCsdlEntityType(EntityMetaData<T> metaData, List<CsdlProperty> properties, List<CsdlPropertyRef> keys) {
        CsdlEntityType csdlEntityType = new CsdlEntityType();

        csdlEntityType
                .setName(metaData.getEntityTypeName())
                .setProperties(properties)
                .setKey(keys);

        return csdlEntityType;
    }

    public static CsdlPropertyRef createCsdlPropertyRef(String name) {
        CsdlPropertyRef csdlPropertyRef = new CsdlPropertyRef();

        csdlPropertyRef.setName(name);

        return csdlPropertyRef;
    }

    public static Property createProperty(String name, ValueType valueType, Object value) {
        return new Property(null, name, valueType, value);
    }

    public static URI createId(String entitySetName, Object id) {
        try {
            return new URI(entitySetName + "(" + String.valueOf(id) + ")");
        } catch (URISyntaxException e) {
            throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
        }
    }

    public static <T> T createInstance(final Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new ODataRuntimeException(ex);
        }
    }
}
