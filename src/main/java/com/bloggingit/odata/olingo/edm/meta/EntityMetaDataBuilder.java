package com.bloggingit.odata.olingo.edm.meta;

import com.bloggingit.odata.edm.annotation.EdmComplexType;
import com.bloggingit.odata.edm.annotation.EdmEntitySet;
import com.bloggingit.odata.edm.annotation.EdmEntityType;
import com.bloggingit.odata.edm.annotation.EdmKey;
import com.bloggingit.odata.edm.annotation.EdmProperty;
import com.bloggingit.odata.edm.enumeration.EdmValueType;
import com.bloggingit.odata.olingo.v4.util.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 *
 * @author mes
 */
public class EntityMetaDataBuilder {

    private static final Class<? extends Annotation> EDM_ENTITY_SET = EdmEntitySet.class;
    private static final Class<? extends Annotation> EDM_ENTITY_TYPE = EdmEntityType.class;
    private static final Class<? extends Annotation> EDM_COMPLEX_TYPE = EdmComplexType.class;
    private static final Class<? extends Annotation> EDM_PROPERTY = EdmProperty.class;
    private static final Class<? extends Annotation> EDM_KEY = EdmKey.class;

    public static EntityMetaDataContainer createContainer(String serviceNamespace, String edmContainer, String dtoPackage) {

        Set<EntityMetaData<?>> metaEntities = createEntityMetaDataList(dtoPackage, serviceNamespace);

        return new EntityMetaDataContainer(serviceNamespace, edmContainer, metaEntities);
    }

    private static <T> Set<EntityMetaData<?>> createEntityMetaDataList(String dtoPackage, String serviceNamespace) {
        Set<EntityMetaData<?>> metaEntities = new HashSet<>();

        Set<Class<?>> odataEntityClasses = ReflectionUtils.findClassesInPackageAnnotatedWith(dtoPackage, EDM_ENTITY_TYPE);
        Set<Class<?>> odataComplexClasses = ReflectionUtils.findClassesInPackageAnnotatedWith(dtoPackage, EDM_COMPLEX_TYPE);

        Stream<Class<?>> odataAllClasses = Stream.concat(odataEntityClasses.stream(), odataComplexClasses.stream());

        odataAllClasses.map((odataClass) -> {
            return createEntityMetaDataFromClass(odataClass, serviceNamespace);
        }).forEachOrdered((entity) -> {
            metaEntities.add(entity);
        });

        return metaEntities;
    }

    private static <T> EntityMetaData<T> createEntityMetaDataFromClass(Class<T> entityClass, String serviceNamespace) {
        boolean isEntitySet = entityClass.isAnnotationPresent(EDM_ENTITY_SET);
        boolean isComplexType = entityClass.isAnnotationPresent(EDM_COMPLEX_TYPE);
        List<EntityMetaProperty> properties = createEntityPropertiesFromClass(entityClass, serviceNamespace);
        return new EntityMetaData<>(entityClass, serviceNamespace, isEntitySet, isComplexType, properties);
    }


    private static <T extends Annotation> List<EntityMetaProperty> createEntityPropertiesFromClass(Class<?> entityClass, String serviceNamespace) {
        List<EntityMetaProperty> newProperties = new ArrayList<>();

        Field[] fields = FieldUtils.getFieldsWithAnnotation(entityClass, EDM_PROPERTY);

        for (Field field : fields) {

            EdmValueType edmValueType = EdmValueType.valueOf(field);

            Class<?> fieldTypeClass = field.getType();

            EdmProperty edmProperty = (EdmProperty) field.getAnnotation(EDM_PROPERTY);

            String edmName = (StringUtils.isNotBlank(edmProperty.name())) ? edmProperty.name() : field.getName();

            EntityMetaProperty propertyData = null;

            boolean isKey = (field.isAnnotationPresent(EDM_KEY));

            if (null != edmValueType)
                switch (edmValueType) {
                    case PRIMITIVE:
                        propertyData = EntityMetaPropertyPrimitve
                                .builder()
                                .isKey(isKey)
                                .name(edmName)
                                .fieldName(field.getName())
                                .fieldType(fieldTypeClass)
                                .maxLength(edmProperty.facets().maxLength())
                                .nullable(edmProperty.facets().nullable())
                                .precision(edmProperty.facets().precision())
                                .scale(edmProperty.facets().scale())
                                .build();
                        break;
                    case ENUM:
                        propertyData = EntityMetaPropertyEnum
                                .builder()
                                .isKey(isKey)
                                .name(edmName)
                                .fieldName(field.getName())
                                .fieldType(fieldTypeClass)
                                .build();
                        break;
                    case COMPLEX:
                        EntityMetaData<?> valueComplexMetaData = createEntityMetaDataFromClass(fieldTypeClass, serviceNamespace);
                        propertyData = EntityMetaPropertyComplex
                                .builder()
                                .name(edmName)
                                .fieldName(field.getName())
                                .fieldType(fieldTypeClass)
                                .valueMetaData(valueComplexMetaData)
                                .build();
                        break;
                    case ENTITY:
                        EntityMetaData<?> valueEntityMetaData = createEntityMetaDataFromClass(fieldTypeClass, serviceNamespace);
                        propertyData = EntityMetaPropertyEntity
                                .builder()
                                .name(edmName)
                                .fieldName(field.getName())
                                .fieldType(fieldTypeClass)
                                .valueMetaData(valueEntityMetaData)
                                .build();
                        break;
                    default:
                        break;
            }

            newProperties.add(propertyData);
        }

        return newProperties;
    }

}
