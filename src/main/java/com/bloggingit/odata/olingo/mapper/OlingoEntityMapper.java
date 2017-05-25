package com.bloggingit.odata.olingo.mapper;

import com.bloggingit.odata.olingo.meta.MetaEntityData;
import com.bloggingit.odata.olingo.meta.MetaEntityPropertyData;
import com.bloggingit.odata.olingo.v4.util.ReflectionUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;

/**
 * Mapper provides methods to convert data into a certain Apache Olingo data
 * structure.
 */
public class OlingoEntityMapper {

    /**
     * Maps names of primitives to their corresponding primitive {@code Class}es
     */
    public final static Map<Class<?>, Class<?>> PRIMITIVE_NAME_MAP = new HashMap<Class<?>, Class<?>>();

    static {
        PRIMITIVE_NAME_MAP.put(boolean.class, Boolean.class);
        PRIMITIVE_NAME_MAP.put(byte.class, Byte.class);
        PRIMITIVE_NAME_MAP.put(short.class, Short.class);
        PRIMITIVE_NAME_MAP.put(char.class, Character.class);
        PRIMITIVE_NAME_MAP.put(int.class, Integer.class);
        PRIMITIVE_NAME_MAP.put(long.class, Long.class);
        PRIMITIVE_NAME_MAP.put(float.class, Float.class);
        PRIMITIVE_NAME_MAP.put(double.class, Double.class);
    }

    public static <T> EntityCollection mapObjectEntitiesToOlingoEntityCollection(Collection<T> objEntities, MetaEntityData<T> metaEntityData) {
        EntityCollection entityCollection = new EntityCollection();

        objEntities.forEach((entity) -> {
            entityCollection
                    .getEntities()
                    .add(mapObjEntityToOlingoEntity(entity, metaEntityData));
        });
        return entityCollection;
    }

    public static <T> Entity mapObjEntityToOlingoEntity(T objEntity, MetaEntityData<T> metaEntityData) {
        final Entity entity = new Entity();

        List<MetaEntityPropertyData> metaProperties = metaEntityData.getProperties();

        metaProperties.forEach((metaProp) -> {
            Object val = ReflectionUtils.invokePropertyGetter(metaProp.getFieldName(), objEntity);
            Property property = new Property(null, metaProp.getName(), metaProp.getValueType(), val);
            entity.addProperty(property);
            if (metaProp.isKey()) {
                entity.setId(createId(metaEntityData.getEntityTypeSetName(), val));
            }
        });

        return entity;
    }

    public static <T> T mapOlingoEntityToObjectEntity(MetaEntityData<T> metaEntityData, Entity entity) {

        T objEntity = createInstance(metaEntityData.getEntityClass());

        List<MetaEntityPropertyData> metaProperties = metaEntityData.getProperties();

        metaProperties.forEach((metaProp) -> {
            Property property = entity.getProperty(metaProp.getName());
            if (property != null) {
                ReflectionUtils.invokePropertySetter(metaProp.getFieldName(), objEntity, property.getValue());
            }
        });


        return objEntity;
    }

    public static <T> CsdlEntityType mapMetaEntityDataToCsdlEntityType(MetaEntityData<T> metaData) {
        Class<T> objEntityClass = metaData.getEntityClass();
        String entityTypeName = metaData.getEntityTypeName();

        CsdlEntityType entityType = null;

        List<CsdlProperty> properties = new ArrayList<>();
        List<CsdlPropertyRef> keys = new ArrayList<>();

        for (MetaEntityPropertyData metaProp : metaData.getProperties()) {
            EdmPrimitiveTypeKind typeKind = convertTypeToEdmPrimTypeKind(metaProp.getFieldType());
            CsdlProperty prop = new CsdlProperty().setName(metaProp.getName()).setType(typeKind.getFullQualifiedName());
            prop.setNullable(metaProp.getNullable());
            if (metaProp.getMaxLength() != -1) {
                prop.setMaxLength(metaProp.getMaxLength());
            }
            if (metaProp.getPrecision() != -1) {
                prop.setPrecision(metaProp.getPrecision());
            }
            if (metaProp.getScale() != -1) {
                prop.setScale(metaProp.getScale());
            }

            Object objEntity = createInstance(objEntityClass);
            Object val = ReflectionUtils.invokePropertyGetter(metaProp.getFieldName(), objEntity);
            if (val != null) {
                prop.setDefaultValue(String.valueOf(val));
            }

            properties.add(prop);

            if (metaProp.isKey()) {
                //create CsdlPropertyRef for Key element
                CsdlPropertyRef propertyRef = new CsdlPropertyRef();
                propertyRef.setName(metaProp.getName());

                keys.add(propertyRef);
            }

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(entityTypeName);
            entityType.setProperties(properties);
            entityType.setKey(keys);
        }

        return entityType;
    }

    public static URI createId(String entitySetName, Object id) {
        try {
            return new URI(entitySetName + "(" + String.valueOf(id) + ")");
        } catch (URISyntaxException e) {
            throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
        }
    }

    private static EdmPrimitiveTypeKind convertTypeToEdmPrimTypeKind(Class<?> type) {
        EdmPrimitiveTypeKind converted;

        if (type.isAssignableFrom(Long.TYPE)) {
            converted = EdmPrimitiveTypeKind.Int64;
        } else if (type.isAssignableFrom(Date.class)) {
            converted = EdmPrimitiveTypeKind.DateTimeOffset;
        } else {
            String typeName;
            if (type.isPrimitive() && PRIMITIVE_NAME_MAP.containsKey(type)) {
                typeName = PRIMITIVE_NAME_MAP.get(type).getSimpleName();
            } else {
                typeName = type.getSimpleName();
            }

            converted = EdmPrimitiveTypeKind.valueOf(typeName);
        }

        return converted;
    }

    private static <T> T createInstance(final Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new ODataRuntimeException(ex);
        }
    }
}
