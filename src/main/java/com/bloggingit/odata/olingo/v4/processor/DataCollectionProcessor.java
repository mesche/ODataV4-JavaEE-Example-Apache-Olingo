package com.bloggingit.odata.olingo.v4.processor;

import com.bloggingit.odata.olingo.edm.meta.EntityMetaData;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaDataContainer;
import com.bloggingit.odata.olingo.v4.callback.ODataErrorCallback;
import com.bloggingit.odata.olingo.v4.service.OlingoDataService;
import java.util.List;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

/**
 * This class is invoked by the Apache Olingo framework when the the OData
 * service is invoked order to display a list/collection of data (entities).
 *
 * This is the case if an EntitySet is requested by the user.
 */
public class DataCollectionProcessor extends AbstractEntityMetaDataProcessor implements EntityCollectionProcessor {

    private OData odata;
    private ServiceMetadata serviceMetadata;
    private final EntityMetaDataContainer entityMetaDataCollection;

    private final OlingoDataService dataService;

    public DataCollectionProcessor(OlingoDataService dataService, EntityMetaDataContainer entityMetaDataCollection) {
        this.dataService = dataService;
        this.entityMetaDataCollection = entityMetaDataCollection;
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, SerializerException {

        // 1st we have retrieve the requested EntitySet from the uriInfo object (representation of the parsed service URI)
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); // the first segment is the EntitySet
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        // 2nd: fetch the data from backend for this requested EntitySetName 
        // it has to be delivered as EntitySet object
        EntityMetaData<?> meta = this.entityMetaDataCollection.getEntityMetaDataByTypeSetName(edmEntitySet.getName());

        EntityCollection entitySet = dataService.getEntityDataList(meta);

        // 3rd: create a serializer based on the requested format (json)
        ODataSerializer serializer = odata.createSerializer(responseFormat);

        // 4th: Now serialize the content: transform from the EntitySet object to InputStream
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();

        final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
        EntityCollectionSerializerOptions opts
                = EntityCollectionSerializerOptions
                        .with()
                        .id(id)
                        .writeContentErrorCallback(new ODataErrorCallback())
                        .contextURL(contextUrl)
                        .build();
        SerializerResult serializedContent = serializer.entityCollection(serviceMetadata, edmEntityType, entitySet, opts);

        // Finally: configure the response object: set the body, headers and status code
        setResponseContentAndOkStatus(response, serializedContent.getContent(), responseFormat);
    }
}
