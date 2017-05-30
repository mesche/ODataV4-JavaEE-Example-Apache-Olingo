package com.bloggingit.odata.model;

import com.bloggingit.odata.edm.annotation.EdmEntitySet;
import com.bloggingit.odata.edm.annotation.EdmEntityType;
import com.bloggingit.odata.edm.annotation.EdmFacets;
import com.bloggingit.odata.edm.annotation.EdmProperty;
import java.util.Date;
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
public class Book extends BaseEntity {

    @EdmProperty(facets = @EdmFacets(nullable = false))
    private String title;

    @EdmProperty(facets = @EdmFacets(maxLength = 2000))
    private String description;

    @EdmProperty
    private Date releaseDate;

    @EdmProperty(name = "writer")
    private String author;

    @EdmProperty
    private Double price;

    @EdmProperty
    private boolean inStock;

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = (releaseDate != null) ? new Date(releaseDate.getTime()) : null;
    }
}
