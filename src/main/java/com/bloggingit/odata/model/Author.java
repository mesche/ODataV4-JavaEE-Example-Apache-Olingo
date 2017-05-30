package com.bloggingit.odata.model;

import com.bloggingit.odata.edm.annotation.EdmEntitySet;
import com.bloggingit.odata.edm.annotation.EdmEntityType;
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

@EdmEntityType
@EdmEntitySet
public class Author extends BaseEntity {

    @EdmProperty(facets = @EdmFacets(nullable = false))
    private String name;

    @EdmProperty(facets = @EdmFacets(nullable = false))
    private Gender gender;
}
