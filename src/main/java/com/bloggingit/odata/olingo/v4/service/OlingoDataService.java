package com.bloggingit.odata.olingo.v4.service;

import com.bloggingit.odata.model.BaseEntity;
import com.bloggingit.odata.exception.EntityDataException;
import com.bloggingit.odata.olingo.v4.mapper.OlingoObjectMapper;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaData;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyData;
import com.bloggingit.odata.olingo.v4.mapper.OlingoPropertyMapper;
import com.bloggingit.odata.storage.InMemoryDataStorage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;

/**
 * This service provides the methods for the OData service to read and store
 * data.
 *
 * Internally the in-memory data storage will be used.
 */
@Stateless
@LocalBean
public class OlingoDataService implements Serializable {

    private static final long serialVersionUID = 1L;

    public <T> EntityCollection getEntityDataList(EntityMetaData<T> entityMetaData) {
        List<T> entityDataList = InMemoryDataStorage.getDataListByBaseEntityClass(entityMetaData.getEntityClass());

        return OlingoObjectMapper.mapObjectEntitiesToOlingoEntityCollection(entityDataList, entityMetaData);

    }

    public <T> Entity getEntityData(EntityMetaData<T> entityMetaData, List<UriParameter> keyParams) {
        long id = Long.parseLong(keyParams.get(0).getText());
        T baseEntity = InMemoryDataStorage.getDataByClassAndId(entityMetaData.getEntityClass(), id);
        return (baseEntity != null) ? OlingoObjectMapper.mapObjEntityToOlingoEntity(baseEntity, entityMetaData) : null;
    }

    @SuppressWarnings("unchecked")
    public void deleteEntityData(Class<?> entityClass, List<UriParameter> keyParams) {
        long id = Long.parseLong(keyParams.get(0).getText());

        InMemoryDataStorage.deleteDataByClassAndId((Class<BaseEntity>) entityClass, id);
    }

    public <T> Entity createEntityData(EntityMetaData<T> entityMetaData, Entity requestEntity) throws ODataApplicationException {

        T baseEntity = OlingoObjectMapper.mapOlingoEntityToObjectEntity(entityMetaData, requestEntity);
        T newBaseEntity;
        try {
            newBaseEntity = InMemoryDataStorage.createEntity(baseEntity);
        } catch (EntityDataException ex) {
            throw new ODataApplicationException("Entity not found",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH, ex);
        }

        return OlingoObjectMapper.mapObjEntityToOlingoEntity(newBaseEntity, entityMetaData);
    }

    public void updateEntityData(EntityMetaData<?> entityMetaData, List<UriParameter> keyParams, Entity entity, HttpMethod httpMethod) throws ODataApplicationException {
        long id = Long.parseLong(keyParams.get(0).getText());

        Map<String, Object> newPropertiesAndValues = new HashMap<>();

        // depending on the HttpMethod, our behavior is different
        // in case of PATCH, the existing property is not touched, do nothing
        //in case of PUT, the existing property is set to null
        boolean nullableUnkownProperties = (httpMethod.equals(HttpMethod.PUT));

        List<EntityMetaPropertyData> metaProperties = entityMetaData.getProperties();


        metaProperties.forEach((metaProp) -> {
            Property newProperty = entity.getProperty(metaProp.getName());

            // the request payload might not consider ALL properties, so it can be null
            if (newProperty != null && !metaProp.isKey()) {

                Object val = OlingoPropertyMapper.mapOlingoPropertyToObjPropertyValue(newProperty, metaProp.getFieldType());

                newPropertiesAndValues.put(metaProp.getFieldName(), val);
            } else if (nullableUnkownProperties && !metaProp.isKey()) {
                // if a property has NOT been added to the request payload
                // depending on the HttpMethod, our behavior is different
                // in case of PUT, the existing property is set to null
                // in case of PATCH, the existing property is not touched, do nothing
                newPropertiesAndValues.put(metaProp.getName(), metaProp.getDefaultValue());
            }
        });

        try {
            InMemoryDataStorage.updateEntity(entityMetaData.getEntityClass(), id, newPropertiesAndValues, nullableUnkownProperties);
        } catch (EntityDataException ex) {
            throw new ODataApplicationException("Entity not found",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH, ex);
        }
    }
}
