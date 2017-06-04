package com.bloggingit.odata.model;

import com.bloggingit.odata.edm.annotation.EdmComplexType;
import com.bloggingit.odata.edm.annotation.EdmFacets;
import com.bloggingit.odata.edm.annotation.EdmProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@EdmComplexType
public class ContactInfo {

    @EdmProperty(facets = @EdmFacets(nullable = false))
    private String eMail;

    @EdmProperty
    private String phone;
}
