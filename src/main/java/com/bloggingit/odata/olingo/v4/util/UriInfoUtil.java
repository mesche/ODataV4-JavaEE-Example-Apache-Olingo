package com.bloggingit.odata.olingo.v4.util;

import java.util.List;
import java.util.Locale;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;

/**
 *
 * @author mes
 */
public class UriInfoUtil {

    public static EdmEntitySet getNavigationTargetEntitySet(EdmEntitySet startEdmEntitySet, EdmNavigationProperty edmNavigationProperty)
            throws ODataApplicationException {

        EdmEntitySet navigationTargetEntitySet = null;

        String navPropName = edmNavigationProperty.getName();
        EdmBindingTarget edmBindingTarget = startEdmEntitySet.getRelatedBindingTarget(navPropName);
        if (edmBindingTarget == null) {
            throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
        }

        if (edmBindingTarget instanceof EdmEntitySet) {
            navigationTargetEntitySet = (EdmEntitySet) edmBindingTarget;
        } else {
            throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
        }

        return navigationTargetEntitySet;
    }
    /**
     * Find the last navigation segment
     *
     * @param uriInfo
     * @return
     */
    public static UriResourceNavigation getLastNavigation(final UriInfoResource uriInfo) {

        final List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        int navigationCount = 1;
        while (navigationCount < resourcePaths.size()
                && resourcePaths.get(navigationCount) instanceof UriResourceNavigation) {
            navigationCount++;
        }

        return (UriResourceNavigation) resourcePaths.get(--navigationCount);
    }

}
