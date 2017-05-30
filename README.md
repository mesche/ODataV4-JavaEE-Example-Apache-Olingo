ODataV4 - JavaEE - Example - Apache Olingo
===========================================

This example application is inspired by the `Basic Tutorial: Create an OData V4 Service with Olingo` which can be found in the [OData 4.0 Java Library Documentation](https://olingo.apache.org/doc/odata4/index.html).
The implementation of the OData service is based on the [Apache Olingo OData 4.0 Library for Java](https://olingo.apache.org/doc/odata4/download.html).

Afterwards the Web Application is deployed on a Java EE server, the OData service can be invoked from a browser or a http client and will provide the data according to the [OData V4 specification](http://www.odata.org/documentation). The service will display a list of books and a few properties that describe each book.

# Infrastructure

- Java 1.8
- Java EE
- Maven
- Apache TomEE
- HTTP Web-Servlet
- Apache Olingo 4.3.0 (2016-09-19)


# Scenario

The OData service in this example will implement the following simple sample model with the `Book` entity.

```java
@EdmEntityType
@EdmEntitySet
public class Book

    @EdmKey
    @EdmProperty(facets = @EdmFacets(nullable = false))
    private long id;

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
```

# Custom Entity Annotations

There is a `Annotation Processor` extension available for the Apache Olingo OData **2.0** library implementation. Unfortunately, there is currently no similar extension for the **4.0** library implementation. That is why I decided to create an own annotation processor, which provided support for elementary requirements for this example.


# Persistence Data Provider

At the moment there is no database connection implemented to provide data for the OData service. To keep it simple, the class `InMemoryDataStorage` provides an in-memory data storage.

# Implemented OData Service Requests

- read Service Document
- read Metadata Document
- read all book data
- read single book data
- read single book property value
- read single book property value (plain text)
- create new book data
- update exisiting book data
- delete existing book data



## Request: Read Service Document

Furthermore, OData specifies the usage of the so-called Service Document. The user can see which entity collections are offered by the OData service.

Request:

```
PATH:   <serviceroot>/
METHOD: GET
```

**Example**

```
http://localhost:8080/odatav4-javaee-example-apache-olingo/api/servlet/v1/odatademo.svc
```

Result:

The expected result is the Service Document which displays informations of the entity container:

```json
{
    "@odata.context": "$metadata",
    "value":
    [
        {
            "name": "BookSet",
            "url": "BookSet"
            }
        ]
    }
```

## Request: Read Metadata Document

According to the OData specification, an OData service has to declare its structure in the so-called Metadata Document. This document defines the contract, such that the user of the service knows which requests can be executed, the structure of the result and how the service can be navigated.

Request:

```
PATH:   <serviceroot>/$metadata
METHOD: GET
```

**Example**

```
http://localhost:8080/odatav4-javaee-example-apache-olingo/api/servlet/v1/odatademo.svc/$metadata
```

Result:

The expected result is the Metadata Document that displays the Schema, EntityType, EntityContainer and the EntitySet.

```xml
<?xml version='1.0' encoding='UTF-8'?>
<edmx:Edmx Version="4.0" xmlns:edmx="http://docs.oasis-open.org/odata/ns/edmx">
    <edmx:DataServices>
        <Schema xmlns="http://docs.oasis-open.org/odata/ns/edm" Namespace="OData">
            <EntityType Name="Book">
            <Key>
                <PropertyRef Name="id"/>
            </Key>
            <Property Name="title" Type="Edm.String" Nullable="false"/>
            <Property Name="description" Type="Edm.String" MaxLength="2000"/>
            <Property Name="releaseDate" Type="Edm.DateTimeOffset"/>
            <Property Name="writer" Type="Edm.String"/>
            <Property Name="price" Type="Edm.Double"/>
            <Property Name="inStock" Type="Edm.Boolean" DefaultValue="false"/>
            <Property Name="id" Type="Edm.Int64" Nullable="false" DefaultValue="0"/>
            </EntityType>
            <EntityContainer Name="Container">
            <EntitySet Name="BookSet" EntityType="OData.Book"/>
            </EntityContainer>
        </Schema>
    </edmx:DataServices>
</edmx:Edmx>
```

## Request: Read Book Entity Collection

This request will display a list of books and some properties that describe each book.

Add the optional `format` parameter to the request url, which contains information about the content type that is requested. This means that the user has the choice to receive the data either in XML or in JSON (default).
If the content type is `application/json;odata.metadata=minimal`, then the payload is formatted in JSON.
The content format can as well be specified via the request header `Accept: application/json;odata.metadata=minimal`.

Internally the `DataCollectionProcessor` implementation of the OData service will be invoked.

Request:

```
PATH:   <serviceroot>/BookSet
METHOD: GET
Header (optional): Accept: application/json;odata.metadata=minimal
```

**Example**

```
http://localhost:8080/odatav4-javaee-example-apache-olingo/api/servlet/v1/odatademo.svc/BookSet

or

http://localhost:8080/odatav4-javaee-example-apache-olingo/api/servlet/v1/odatademo.svc/BookSet?$format=xml

or

http://localhost:8080/odatav4-javaee-example-apache-olingo/api/servlet/v1/odatademo.svc/BookSet?$format=application/json;odata.metadata=minimal
```

Result:

The expected result is the list of book entries:

```json
{
    "@odata.context": "$metadata#BookSet",
    "value":
    [
        {
            "title": "Book Title 1",
            "description": "This is the description of book 1",
            "releaseDate": "2011-07-21T00:00:00+02:00",
            "writer": "Author 1",
            "price": 9.95,
            "inStock": true,
            "id": 1
        },
        {
            "title": "Book Title 2",
            "description": "This is the description of book 2",
            "releaseDate": "2015-08-06T13:15:00+02:00",
            "writer": "Author 2",
            "price": 5.99,
            "inStock": true,
            "id": 2
        },
        {
            "title": "Book Title 3",
            "description": "This is the description of book 3",
            "releaseDate": "2013-05-12T00:00:00+02:00",
            "writer": "Author 3",
            "price": 14.5,
            "inStock": false,
            "id": 3
        }
    ]
}
```


## Request: Read Single Book Entity

This request will display the details of a single book entity, which has the corresponding ID.

Internally the `DataEntityProcessor` implementation of the OData service will be invoked.

Request:

```
PATH:   <serviceroot>/BookSet(ID_OF_THE_BOOK)    or  <serviceroot>/BookSet(id=ID_OF_THE_BOOK)
METHOD: GET
Header (optional): Accept: application/json;odata.metadata=minimal
```

**Example**

```
http://localhost:8080/odatav4-javaee-example-apache-olingo/api/servlet/v1/odatademo.svc/BookSet(1)
```

Result:

The expected result is a response with the details of a single book with the id 1.


```json
{
    "@odata.context": "$metadata#BookSet",
    "title": "Book Title 1",
    "description": "This is the description of book 1",
    "releaseDate": "2011-07-21T00:00:00+02:00",
    "writer": "Author 1",
    "price": 9.95,
    "inStock": true,
    "id": 1
}
```

## Request: Read Single Book Property

If you doesn’t want to receive the full payload of the entity, you can use this request to receive only the value of the property of the OData model you needed.

Internally the `DataPrimitiveProcessor` or `DataPrimitiveValueProcessor` implementation of the OData service will be invoked.

Request:

```
PATH:   <serviceroot>/BookSet(ID_OF_THE_BOOK)/PROPERTY_NAME
METHOD: GET
Header (optional): Accept: application/json;odata.metadata=minimal
```

**Example**

```
http://localhost:8080/odatav4-javaee-example-apache-olingo/api/servlet/v1/odatademo.svc/BookSet(1)/title
```

Result:

The expected result is a response with the title value of the book with the id 1.

```json
{
    "@odata.context": "$metadata#BookSet/title",
    "value": "Book Title 1"
}
```

### Plain Text Value

If you use the `DataPrimitiveValueProcessor` implementation, it is also possible to request only the pure plain text value of a property.

Request:

```
PATH:   <serviceroot>/BookSet(ID_OF_THE_BOOK)/PROPERTY_NAME/$value
METHOD: GET
Header (optional): Accept: application/json;odata.metadata=minimal
```

**Example**

```
http://localhost:8080/odatav4-javaee-example-apache-olingo/api/servlet/v1/odatademo.svc/BookSet(1)/title/$value
```

Result:

The expected result is a response with only the pure plain text value of the title property.

```text
Book Title 1
```

## Request: Create New Book

With this request we can create a new book and add it to the list of the available books.
The Olingo library takes this request, serializes the request body and invokes the corresponding method of our processor class. 

Internally the `DataEntityProcessor` implementation of the OData service will be invoked.

Request:

```
PATH:   <serviceroot>/BookSet
METHOD: POST
Header: Content-Type: application/json;odata.metadata=minimal
Body:   JSON data
```

**Example**

```
http://localhost:8080/odatav4-javaee-example-apache-olingo/api/servlet/v1/odatademo.svc/BookSet
```

Request Body:

```json
{
    "title": "Book Title New",
    "description": "This is the description of the new book",
    "releaseDate": "2017-04-21T00:00:00+02:00",
    "writer": "Author New",
    "price": 11.95,
    "inStock": true
}
```

```json
{
    "name": "Author New",
    "gender": "FEMALE"
}
```

Result:

The result is a response with the details of the new book with the new assigned id.

```json
{
    "title": "Book Title New",
    "description": "This is the description of the new book",
    "releaseDate": "2017-04-21T00:00:00+02:00",
    "writer": "Author New",
    "price": 11.95,
    "inStock": true,
    "id": 4
}
```

## Request: Update Existing Book

With this request we can update the values of an existing book. The update of an entity can be realized either with a `PUT` or a `PATCH` request.

**PUT**: The value of the property is updated in the backend. The value of the other properties is set to null (exception: key properties can never be null).

**PATCH**: The value of the property is updated in the backend. The values of the other properties remain untouched.

The difference becomes relevant only in case if the user doesn’t send all the properties in the request body.

Internally the `DataEntityProcessor` implementation of the OData service will be invoked.

Request:

```
PATH:   <serviceroot>/BookSet(ID_OF_THE_BOOK)
METHOD: PUT or PATCH
Header: Content-Type: application/json;odata.metadata=minimal
Body:   JSON data
```

**Example**

```
http://localhost:8080/odatav4-javaee-example-apache-olingo/api/servlet/v1/odatademo.svc/BookSet(2)
```

Request Body:

```json
{
    "title": "Book Title 2 Updated",
    "description": "This is the description of book 2 Updated",
    "writer": "Author 2 Updated"
}
```

Result:

The OData service is not expected to return any response payload (HTTP status code to 204 – no content).


## Delete Existing Book

With this request we can remove data record of an existing book.

Internally the `DataEntityProcessor` implementation of the OData service will be invoked.

Request:

```
PATH:   <serviceroot>/BookSet(ID_OF_THE_BOOK)
METHOD: DELETE
```

**Example**

```
http://localhost:8080/odatav4-javaee-example-apache-olingo/api/servlet/v1/odatademo.svc/BookSet(2)
```

Result:

The OData service is not expected to return any response payload (HTTP status code to 204 – no content).



----------------------------------
Markus Eschenbach
[www.blogging-it.com](http://www.blogging-it.com)

