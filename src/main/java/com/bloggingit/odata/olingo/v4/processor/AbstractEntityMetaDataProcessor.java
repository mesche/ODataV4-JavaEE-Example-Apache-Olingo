package com.bloggingit.odata.olingo.v4.processor;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceProperty;

/**
 *
 * @author mes
 */
public abstract class AbstractEntityMetaDataProcessor {

    protected void setResponseContentAndOkStatus(ODataResponse response, InputStream content, ContentType responseFormat) {
        response.setContent(content);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    protected void setResponseNoContentStatus(ODataResponse response) {
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    }

    protected EdmEntitySet getUriResourceEdmEntitySet(UriInfoResource uriInfo) throws ODataApplicationException {
        return getUriResourceEntitySet(uriInfo).getEntitySet();
    }

    protected List<UriParameter> getUriResourceKeyPredicates(UriInfoResource uriInfo) throws ODataApplicationException {
        return getUriResourceEntitySet(uriInfo).getKeyPredicates();
    }

    protected UriResourceEntitySet getUriResourceEntitySet(UriInfoResource uriInfo) throws ODataApplicationException {
        List<UriResource> resourceParts = uriInfo.getUriResourceParts();
        // To get the entity set we have to interpret all URI segments
        if (!(resourceParts.get(0) instanceof UriResourceEntitySet)) {
            // Here we should interpret the whole URI but in this example we do not support navigation so we throw an exception
            throw new ODataApplicationException("Invalid resource type for first segment.", HttpStatusCode.NOT_IMPLEMENTED
                    .getStatusCode(), Locale.ENGLISH);
        }

        return (UriResourceEntitySet) resourceParts.get(0);
    }

    protected EdmProperty getUriResourceEdmProperty(UriInfoResource uriInfo) throws ODataApplicationException {

        List<UriResource> resourceParts = uriInfo.getUriResourceParts();
        // the last segment is the Property
        int idx = resourceParts.size() - 1;

        if (!(resourceParts.get(idx) instanceof UriResourceProperty)) {
            // Here we should interpret the whole URI but in this example we do not support navigation so we throw an exception
            throw new ODataApplicationException("Invalid resource type for last segment.", HttpStatusCode.NOT_IMPLEMENTED
                    .getStatusCode(), Locale.ENGLISH);
        }

        UriResourceProperty uriProperty = (UriResourceProperty) resourceParts.get(idx);
        return uriProperty.getProperty();
    }
}
