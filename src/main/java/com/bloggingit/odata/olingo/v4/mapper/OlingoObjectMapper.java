package com.bloggingit.odata.olingo.v4.mapper;

import com.bloggingit.odata.olingo.edm.meta.EntityMetaData;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaProperty;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyComplex;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyEntity;
import com.bloggingit.odata.olingo.v4.factory.OlingoObjectFactory;
import com.bloggingit.odata.olingo.v4.util.ReflectionUtils;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.EnumUtils;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Linked;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlStructuralType;

/**
 * Mapper provides methods to convert data into a certain Apache Olingo data
 * structure.
 */
public class OlingoObjectMapper {

    public static <T> EntityCollection mapObjectEntitiesToOlingoEntityCollection(Collection<T> objEntities, EntityMetaData<T> entityMetaData) {
        EntityCollection entityCollection = new EntityCollection();
        entityCollection.setCount(objEntities.size());

        objEntities.forEach((entity) -> {
            entityCollection
                    .getEntities()
                    .add(mapObjEntityToOlingoEntity(entityMetaData, entity));
        });
        return entityCollection;
    }

    public static <T> Entity mapObjEntityToOlingoEntity(EntityMetaData<?> entityMetaData, Object objEntity) {

        List<EntityMetaProperty> metaProperties = entityMetaData.getProperties();

        List<Property> properties = new ArrayList<>();
        List<Link> navigationLinks = new ArrayList<>();

        URI keyId = null;
        for (EntityMetaProperty metaProp : metaProperties) {
            if (metaProp instanceof EntityMetaPropertyEntity) {
                Object metaPropValue = ReflectionUtils.invokePropertyGetter(metaProp.getFieldName(), objEntity);
                if (metaPropValue != null) {
                    EntityMetaData<?> valueMetaData = ((EntityMetaPropertyEntity) metaProp).getValueMetaData();
                    Entity subEntity = mapObjEntityToOlingoEntity(valueMetaData, metaPropValue);
                    navigationLinks.add(OlingoObjectFactory.createLink(metaProp.getEdmName(), subEntity));
                }
            } else {
                Property property = mapMetaPropertyDataToOlingoProperty(objEntity, metaProp);
                properties.add(property);

                if (metaProp.isKey()) {
                    keyId = OlingoObjectFactory.createId(entityMetaData.getEntityTypeSetName(), property.getValue());
                }
            }
        }

        return OlingoObjectFactory.createEntity(properties, navigationLinks, keyId);
    }

    public static <T> T mapOlingoEntityToObjectEntity(EntityMetaData<T> entityMetaData, Linked linkedEntity) {

        T objEntity = OlingoObjectFactory.createInstance(entityMetaData.getEntityClass());

        List<EntityMetaProperty> metaProperties = entityMetaData.getProperties();

        metaProperties.forEach((metaProp) -> {
            Object val = null;
            if (metaProp instanceof EntityMetaPropertyEntity) {
                EntityMetaData<?> propEntityMetaData = ((EntityMetaPropertyEntity) metaProp).getValueMetaData();
                Link navigationLink = linkedEntity.getNavigationLink(metaProp.getEdmName());
                Entity propInlineEntity = navigationLink.getInlineEntity();
                val = mapOlingoEntityToObjectEntity(propEntityMetaData, propInlineEntity);
            } else if (metaProp instanceof EntityMetaPropertyComplex) {
                EntityMetaData<?> propComplexMetaData = ((EntityMetaPropertyComplex) metaProp).getValueMetaData();
                Property property = ((Entity) linkedEntity).getProperty(metaProp.getEdmName());
                if (property != null) {
                    val = mapOlingoEntityToObjectEntity(propComplexMetaData, (ComplexValue) property.getValue());
                }
            } else if (linkedEntity instanceof Entity) {
                Property property = ((Entity) linkedEntity).getProperty(metaProp.getEdmName());
                val = mapOlingoPropertyToObjValue(property, metaProp);
            } else if (linkedEntity instanceof ComplexValue) {
                for (Property property : ((ComplexValue) linkedEntity).getValue()) {
                    if (metaProp.getEdmName().equals(property.getName())) {
                        val = mapOlingoPropertyToObjValue(property, metaProp);
                        break;
                    }
                }
            }
            if (val != null) {
                ReflectionUtils.invokePropertySetter(metaProp.getFieldName(), objEntity, val);
            }
        });

        return objEntity;
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends Enum<E>> List<CsdlEnumType> mapEntityMetaDataToCsdlEnumTypeList(final EntityMetaData<T> meta) {
        List<CsdlEnumType> enumTypes = new ArrayList<>();

        meta.getEnumPropertyData().forEach((prop) -> {
            enumTypes.add(OlingoObjectFactory.createEnumType((Class<E>) prop.getFieldType()));
        });

        return enumTypes;
    }

    public static <T> CsdlStructuralType mapEntityMetaDataToCsdlStructuralType(EntityMetaData<T> metaData, String serviceNamespace) {
        Class<T> objEntityClass = metaData.getEntityClass();

        List<CsdlProperty> properties = new ArrayList<>();
        List<CsdlPropertyRef> keys = new ArrayList<>();
        List<CsdlNavigationProperty> navigations = new ArrayList<>();

        metaData.getProperties().stream().map((metaProp) -> {

            FullQualifiedName typeFQN = FQNMapper.mapToPropertyValueTypeFQN(serviceNamespace, metaProp);
            Object objEntity = OlingoObjectFactory.createInstance(objEntityClass);

            if (metaProp instanceof EntityMetaPropertyEntity) { // add navigation ref only
                EntityMetaPropertyEntity metaPropEntity = (EntityMetaPropertyEntity) metaProp;
                navigations.add(OlingoObjectFactory.createCsdlNavigationProperty(typeFQN, metaPropEntity.getEdmName(), metaPropEntity.getNullable(), null));
            } else {
                Object val = ReflectionUtils.invokePropertyGetter(metaProp.getFieldName(), objEntity);
                properties.add(OlingoObjectFactory.createCsdlProperty(metaProp, typeFQN, String.valueOf(val)));
            }

            return metaProp;
        }).filter((metaProp) -> (metaProp.isKey())).forEachOrdered((metaProp) -> {
            keys.add(OlingoObjectFactory.createCsdlPropertyRef(metaProp.getEdmName()));
        });

        CsdlStructuralType structuralType;
        if (metaData.isComplexType()) {
            structuralType = OlingoObjectFactory.createCsdlComplexType(metaData.getEntityTypeName(), properties);
        } else {
            structuralType = OlingoObjectFactory.createCsdlEntityType(metaData.getEntityTypeName(), properties, navigations, keys);
        }

        return structuralType;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> Object mapOlingoPropertyToObjValue(Property property, EntityMetaProperty metaProp) {

        Class<?> fieldType = metaProp.getFieldType();

        Object val = null;
        if (property != null) {
            val = property.getValue();
            if (val != null && !fieldType.equals(val.getClass())) {
                if (property.isEnum()) {
                    val = EnumUtils.getEnumList((Class<E>) fieldType).get(Integer.parseInt(val.toString()));
                } else if (val instanceof Calendar) {
                    val = ((Calendar) val).getTime();
                }
            }
        }
        return val;
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends Enum<E>> Property mapMetaPropertyDataToOlingoProperty(T objEntity, EntityMetaProperty metaProp) {
        Object val = ReflectionUtils.invokePropertyGetter(metaProp.getFieldName(), objEntity);
        ValueType valueType = OlingoTypeMapper.mapToValueType(metaProp);
        Object oDataValue = null;
        if (val != null) {
            if (null == valueType) { //primitive fallback
                oDataValue = val;
            } else {
                switch (valueType) {
                    case ENUM:
                        oDataValue = EnumUtils.getEnum((Class<E>) metaProp.getFieldType(), String.valueOf(val)).ordinal();
                        break;
                    case COMPLEX:
                        oDataValue = mapMetaPropertyValueToOlingoLinked(val, metaProp);
                        break;
                    case ENTITY:
                        throw new IllegalArgumentException("Can't create entity property. Use navigation ref instead");
                    default:
                        //primitive fallback
                        oDataValue = val;
                        break;
                }
            }
        }

        return OlingoObjectFactory.createProperty(metaProp.getEdmName(), valueType, oDataValue);
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends Enum<E>> Linked mapMetaPropertyValueToOlingoLinked(Object value, EntityMetaProperty metaProp) {
        Linked valuable = null;

        if (value != null && metaProp instanceof EntityMetaPropertyComplex) {
            EntityMetaPropertyComplex propComplex = (EntityMetaPropertyComplex) metaProp;

            List<Property> subProperties = new ArrayList<>();
            propComplex.getValueMetaData().getProperties().forEach((subMetaProp) -> {
                subProperties.add(mapMetaPropertyDataToOlingoProperty(value, subMetaProp));
            });

            valuable = OlingoObjectFactory.createComplexValue(subProperties);
        }
        return valuable;
    }
}
