package com.bloggingit.odata.olingo.v4.processor;

import com.bloggingit.odata.olingo.edm.meta.EntityMetaData;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaDataContainer;
import com.bloggingit.odata.olingo.v4.service.OlingoDataService;
import com.bloggingit.odata.olingo.v4.util.OlingoUtil;
import java.io.InputStream;
import java.util.List;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

/**
 * This class is invoked by the Apache Olingo framework when the the OData
 * service is invoked order to display the data of a entity.
 *
 * This is the case if an Entity is requested by the user.
 */
public class DataEntityProcessor extends AbstractEntityMetaDataProcessor implements EntityProcessor {

    private OData odata;
    private ServiceMetadata serviceMetadata;
    private final EntityMetaDataContainer entityMetaDataCollection;

    private final OlingoDataService dataService;

    public DataEntityProcessor(OlingoDataService dataService, EntityMetaDataContainer entityMetaDataCollection) {
        this.dataService = dataService;
        this.entityMetaDataCollection = entityMetaDataCollection;
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

        // 1. retrieve the Entity Type
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        // Note: only in our example we can assume that the first segment is the EntitySet
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        // 2. retrieve the data from backend
        EntityMetaData<?> meta = this.entityMetaDataCollection.getEntityMetaDataByTypeSetName(edmEntitySet.getName());

        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        Entity entity = this.dataService.getEntityData(meta, keyPredicates);

        // 3. serialize
        EdmEntityType entityType = edmEntitySet.getEntityType();

        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
        // expand and select currently not supported
        EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

        ODataSerializer serializer = odata.createSerializer(responseFormat);
        SerializerResult serializerResult = serializer.entity(serviceMetadata, entityType, entity, options);
        InputStream entityStream = serializerResult.getContent();

        //4. configure the response object
        setResponseContentAndOkStatus(response, entityStream, responseFormat);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
            ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

        // 1. Retrieve the entity type from the URI
        EdmEntitySet edmEntitySet = OlingoUtil.getEdmEntitySet(uriInfo);
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // 2. create the data in backend
        // 2.1. retrieve the payload from the POST request for the entity to create and deserialize it
        InputStream requestInputStream = request.getBody();
        ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
        DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
        Entity requestEntity = result.getEntity();
        // 2.2 do the creation in backend, which returns the newly created entity
        EntityMetaData<?> meta = this.entityMetaDataCollection.getEntityMetaDataByTypeSetName(edmEntitySet.getName());

        Entity createdEntity = this.dataService.createEntityData(meta, requestEntity);

        // 3. serialize the response (we have to return the created entity)
        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
        // expand and select currently not supported
        EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

        ODataSerializer serializer = this.odata.createSerializer(responseFormat);
        SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity, options);

        //4. configure the response object
        setResponseContentAndOkStatus(response, serializedResponse.getContent(), responseFormat);
    }

    @Override
    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
            ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

        // 1. Retrieve the entity set which belongs to the requested entity
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        // Note: only in our example we can assume that the first segment is the EntitySet
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // 2. update the data in backend
        // 2.1. retrieve the payload from the PUT request for the entity to be updated
        InputStream requestInputStream = request.getBody();
        ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
        DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
        Entity requestEntity = result.getEntity();
        // 2.2 do the modification in backend
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        // Note that this updateEntity()-method is invoked for both PUT or PATCH operations
        HttpMethod httpMethod = request.getMethod();

        EntityMetaData<?> meta = this.entityMetaDataCollection.getEntityMetaDataByTypeSetName(edmEntitySet.getName());

        this.dataService.updateEntityData(meta, keyPredicates, requestEntity, httpMethod);

        //3. configure the response object
        setResponseNoContentStatus(response);
    }

    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {

        // 1. Retrieve the entity set which belongs to the requested entity
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        // Note: only in our example we can assume that the first segment is the EntitySet
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        // 2. delete the data in backend
        EntityMetaData<?> meta = this.entityMetaDataCollection.getEntityMetaDataByTypeSetName(edmEntitySet.getName());

        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        this.dataService.deleteEntityData(meta.getEntityClass(), keyPredicates);

        //3. configure the response object
        setResponseNoContentStatus(response);
    }

}
