package com.bloggingit.odata.olingo.edm.meta;

import com.bloggingit.odata.edm.annotation.EdmEntitySet;
import com.bloggingit.odata.edm.annotation.EdmEntityType;
import com.bloggingit.odata.edm.annotation.EdmKey;
import com.bloggingit.odata.edm.annotation.EdmProperty;
import com.bloggingit.odata.edm.enumeration.EdmValueType;
import com.bloggingit.odata.olingo.v4.util.ReflectionUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 *
 * @author mes
 */
public class EntityMetaDataFactory {

    public static <T> Set<EntityMetaData<?>> createEntityMetaDataList(String dtoPackage, String serviceNamespace) {
        Set<EntityMetaData<?>> metaEntities = new HashSet<>();

        Set<Class<?>> odataEntityClasses = ReflectionUtils.findClassesInPackageAnnotatedWith(dtoPackage, EdmEntityType.class);

        odataEntityClasses.stream().map((entityClass) -> {
            boolean isEntitySet = entityClass.isAnnotationPresent(EdmEntitySet.class);
            List<EntityMetaPropertyData> properties = createEntityPropertiesFromClass(entityClass, serviceNamespace);
            EntityMetaData<?> entity = new EntityMetaData<>(entityClass, serviceNamespace, isEntitySet, properties);
            return entity;
        }).forEachOrdered((entity) -> {
            metaEntities.add(entity);
        });

        return metaEntities;
    }

    public static List<EntityMetaPropertyData> createEntityPropertiesFromClass(Class<?> entityClass, String serviceNamespace) {
        List<EntityMetaPropertyData> newProperties = new ArrayList<>();

        Field[] fields = FieldUtils.getFieldsWithAnnotation(entityClass, EdmProperty.class);

        for (Field field : fields) {

            EdmProperty edmProperty = field.getAnnotation(EdmProperty.class);

            boolean isKey = (field.isAnnotationPresent(EdmKey.class));

            String name = (StringUtils.isNotBlank(edmProperty.name())) ? edmProperty.name() : field.getName();

            EdmValueType edmValueType = (field.getType().isEnum()) ? EdmValueType.ENUM : EdmValueType.PRIMITIVE;

            EntityMetaPropertyData propertyData = EntityMetaPropertyData
                    .builder()
                    .name(name)
                    .fieldName(field.getName())
                    .fieldType(field.getType())
                    .isKey(isKey)
                    .maxLength(edmProperty.facets().maxLength())
                    .nullable(edmProperty.facets().nullable())
                    .precision(edmProperty.facets().precision())
                    .scale(edmProperty.facets().scale())
                    .valueType(edmValueType)
                    .build();

            newProperties.add(propertyData);
        }

        return newProperties;
    }
}
