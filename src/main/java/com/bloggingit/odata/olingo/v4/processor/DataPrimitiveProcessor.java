package com.bloggingit.odata.olingo.v4.processor;

import com.bloggingit.odata.olingo.edm.meta.EntityMetaData;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaDataContainer;
import com.bloggingit.odata.olingo.v4.service.OlingoDataService;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;

/**
 * This class is invoked by the Apache Olingo framework when the the OData
 * service is invoked order to display primitive property data of a entity.
 */
public class DataPrimitiveProcessor extends AbstractEntityMetaDataProcessor implements PrimitiveProcessor {

    private OData odata;
    private ServiceMetadata serviceMetadata;
    private final EntityMetaDataContainer entityMetaDataCollection;

    private final OlingoDataService dataService;

    public DataPrimitiveProcessor(OlingoDataService dataService, EntityMetaDataContainer entityMetaDataCollection) {
        this.dataService = dataService;
        this.entityMetaDataCollection = entityMetaDataCollection;
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readPrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        // 1. Retrieve info from URI
        // 1.1. retrieve the info about the requested entity set
        EdmEntitySet edmEntitySet = getUriResourceEdmEntitySet(uriInfo);
        // the key for the entity
        List<UriParameter> keyPredicates = getUriResourceKeyPredicates(uriInfo);

        // 1.2. retrieve the requested (Edm) property
        EdmProperty edmProperty = getUriResourceEdmProperty(uriInfo);
        String edmPropertyName = edmProperty.getName();
        // in our example, we know we have only primitive types in our model
        EdmPrimitiveType edmPropertyType = (EdmPrimitiveType) edmProperty.getType();

        // 2. retrieve data from backend
        // 2.1. retrieve the entity data, for which the property has to be read
        EntityMetaData<?> meta = this.entityMetaDataCollection.getEntityMetaDataByTypeSetName(edmEntitySet.getName());

        Entity entity = this.dataService.getEntityData(meta, keyPredicates);

        if (entity == null) { // Bad request
            throw new ODataApplicationException("Entity not found",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }

        // 2.2. retrieve the property data from the entity
        Property property = entity.getProperty(edmPropertyName);
        if (property == null) {
            throw new ODataApplicationException("Property not found",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }

        // 3. serialize
        Object value = property.getValue();
        if (value != null) {
            // 3.1. configure the serializer
            ODataSerializer serializer = odata.createSerializer(responseFormat);

            ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).navOrPropertyPath(edmPropertyName).build();
            PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();
            // 3.2. serialize
            SerializerResult serializerResult = serializer.primitive(serviceMetadata, edmPropertyType, property, options);
            InputStream propertyStream = serializerResult.getContent();

            //4. configure the response object
            setResponseContentAndOkStatus(response, propertyStream, responseFormat);
        } else {
            // in case there's no value for the property, we can skip the serialization
            setResponseNoContentStatus(response);
        }
    }

    @Override
    public void updatePrimitive(ODataRequest odr, ODataResponse odr1, UriInfo ui, ContentType ct, ContentType ct1) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deletePrimitive(ODataRequest odr, ODataResponse odr1, UriInfo ui) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
