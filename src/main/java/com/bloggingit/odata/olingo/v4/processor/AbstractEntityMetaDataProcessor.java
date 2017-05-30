package com.bloggingit.odata.olingo.v4.processor;

import java.io.InputStream;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataResponse;

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

}
