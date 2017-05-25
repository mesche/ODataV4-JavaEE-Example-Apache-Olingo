package com.bloggingit.odata.model;

import com.bloggingit.odata.olingo.annotation.edm.EdmFacets;
import com.bloggingit.odata.olingo.annotation.edm.EdmKey;
import com.bloggingit.odata.olingo.annotation.edm.EdmProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {

    @EdmKey
    @EdmProperty(facets = @EdmFacets(nullable = false))
    private long id;
}
