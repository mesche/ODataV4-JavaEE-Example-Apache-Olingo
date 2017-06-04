package com.bloggingit.odata.olingo.v4.factory;

import com.bloggingit.odata.olingo.edm.meta.EntityMetaData;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaProperty;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyEntity;
import com.bloggingit.odata.olingo.edm.meta.EntityMetaPropertyPrimitve;
import com.bloggingit.odata.olingo.v4.mapper.FQNMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.EnumUtils;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;

/**
 *
 * @author mes
 */
public class OlingoObjectFactory {

    public static <T> CsdlEntitySet createCsdlEntitySet(EntityMetaData<T> meta, String serviceNamespace) {
        CsdlEntitySet csdlEntitySet = new CsdlEntitySet();

        csdlEntitySet.setName(meta.getEntityTypeSetName());

        csdlEntitySet.setType(FQNMapper.createFullQualifiedName(serviceNamespace, meta));

        List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<>();
        for (EntityMetaProperty metaProp : meta.getProperties()) {
            if (metaProp instanceof EntityMetaPropertyEntity) { // add navigation ref only
                EntityMetaPropertyEntity metaPropEntity = (EntityMetaPropertyEntity) metaProp;
                navPropBindingList.add(createCsdlNavigationPropertyBinding(metaPropEntity.getEdmName(), metaPropEntity.getValueMetaData().getEntityTypeSetName()));
            }

        }
        csdlEntitySet.setNavigationPropertyBindings(navPropBindingList);


        return csdlEntitySet;
    }

    public static CsdlEntityContainer createCsdlEntityContainer(String containerName, List<CsdlEntitySet> entitySets) {
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();

        entityContainer.setName(containerName);

        entityContainer.setEntitySets(entitySets);

        return entityContainer;
    }

    public static CsdlEntityContainerInfo createCsdlEntityContainerInfo(String serviceNamespace, String containerName) {
        CsdlEntityContainerInfo csdlEntityContainerInfo = new CsdlEntityContainerInfo();

        FullQualifiedName containerNameFQ = FQNMapper.createFullQualifiedName(serviceNamespace, containerName);

        csdlEntityContainerInfo.setContainerName(containerNameFQ);

        return csdlEntityContainerInfo;
    }

    public static CsdlSchema createCsdlSchema(String serviceNamespace, CsdlEntityContainer entityContainer, List<CsdlEntityType> entityTypes, List<CsdlEnumType> enumTypes, List<CsdlComplexType> complexTypes) {
        CsdlSchema csdlSchema = new CsdlSchema();

        csdlSchema.setNamespace(serviceNamespace);
        csdlSchema.setEntityContainer(entityContainer);
        csdlSchema.setEntityTypes(entityTypes);
        csdlSchema.setEnumTypes(enumTypes);
        csdlSchema.setComplexTypes(complexTypes);

        return csdlSchema;
    }

    public static CsdlProperty createCsdlProperty(EntityMetaProperty metaProp, FullQualifiedName typeFQN, String defaultVal) {
        CsdlProperty csdlProperty = new CsdlProperty();

        csdlProperty.setName(metaProp.getEdmName()).setType(typeFQN);

        if (metaProp instanceof EntityMetaPropertyPrimitve) {

            EntityMetaPropertyPrimitve primitiveProp = (EntityMetaPropertyPrimitve) metaProp;

            csdlProperty
                    .setNullable(metaProp.getNullable());

            if (primitiveProp.getMaxLength() != -1) {
                csdlProperty.setMaxLength(primitiveProp.getMaxLength());
            }
            if (primitiveProp.getPrecision() != -1) {
                csdlProperty.setPrecision(primitiveProp.getPrecision());
            }
            if (primitiveProp.getScale() != -1) {
                csdlProperty.setScale(primitiveProp.getScale());
            }
            if (defaultVal != null) {
                csdlProperty.setDefaultValue(defaultVal);
            }
        }

        return csdlProperty;
    }
    /**
     *
     * @param path target entitySet, where the nav prop points to
     * @param target target entitySet, where the nav prop points to
     * @return a CsdlNavigationPropertyBinding for the entity set
     */
    public static CsdlNavigationPropertyBinding createCsdlNavigationPropertyBinding(String path, String target) {
        CsdlNavigationPropertyBinding csdlNavigationPropertyBinding = new CsdlNavigationPropertyBinding();
        csdlNavigationPropertyBinding.setPath(path);
        csdlNavigationPropertyBinding.setTarget(target);
        return csdlNavigationPropertyBinding;
    }

    public static <E extends Enum<E>> CsdlEnumType createEnumType(final Class<E> enumClass) {

        List<E> enumList = EnumUtils.getEnumList(enumClass);

        List<CsdlEnumMember> csdlEnumMember = new ArrayList<>();

        enumList.forEach((entry) -> {
            csdlEnumMember.add(new CsdlEnumMember().setName(entry.name()).setValue(String.valueOf(entry.ordinal())));
        });

        return new CsdlEnumType()
                .setName(enumClass.getSimpleName())
                .setMembers(csdlEnumMember)
                .setUnderlyingType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
    }

    public static <T> CsdlEntityType createCsdlEntityType(String entityTypeName, List<CsdlProperty> properties, List<CsdlNavigationProperty> csdlNavigationProperty, List<CsdlPropertyRef> keys) {
        CsdlEntityType csdlEntityType = new CsdlEntityType();

        csdlEntityType
                .setName(entityTypeName)
                .setProperties(properties)
                .setNavigationProperties(csdlNavigationProperty)
                .setKey(keys);

        return csdlEntityType;
    }

    public static <T> CsdlComplexType createCsdlComplexType(String entityTypeName, List<CsdlProperty> properties) {
        CsdlComplexType csdlComplexType = new CsdlComplexType();

        csdlComplexType
                .setName(entityTypeName)
                .setProperties(properties);

        return csdlComplexType;
    }

    public static CsdlPropertyRef createCsdlPropertyRef(String name) {
        CsdlPropertyRef csdlPropertyRef = new CsdlPropertyRef();

        csdlPropertyRef.setName(name);

        return csdlPropertyRef;
    }

    // https://olingo.apache.org/doc/odata4/tutorials/navigation/tutorial_navigation.html
    /**
     *
     * @param typeFQ fully qualified name of the entity type to which we’re
     * navigating.
     * @param name the name of the navigation property is used as segment in the
     * URI.
     * @param nullable if the navigation target is required. the default is
     * assumed to be “true”.
     * @param partner An attribute, used to define a bi-directional
     * relationship. Specifies a path from the entity type to the navigation
     * property. In our example, we can navigate from book to author and from
     * author to book
     * @return a navigation property
     */
    public static CsdlNavigationProperty createCsdlNavigationProperty(FullQualifiedName typeFQ, String name, Boolean nullable, String partner) {
        boolean isNullable = (nullable == null) ? true : nullable;
        return new CsdlNavigationProperty()
                .setName(name)
                .setType(typeFQ)
                .setNullable(isNullable)
                .setPartner(partner);
    }


    public static ComplexValue createComplexValue(List<Property> properties) {
        ComplexValue complexValue = new ComplexValue();
        List<Property> complexSubValues = complexValue.getValue();
        complexSubValues.addAll(properties);

        return complexValue;
    }

    public static Property createProperty(String name, ValueType valueType, Object value) {
        return new Property(null, name, valueType, value);
    }

    public static Entity createEntity(List<Property> properties, List<Link> navigationLinks, URI key) {
        Entity entity = new Entity();
        entity.getProperties().addAll(properties);
        entity.getNavigationLinks().addAll(navigationLinks);
        entity.setId(key);

        return entity;
    }

    public static Link createLink(String title, Entity entity) {
        Link link = new Link();
        link.setTitle(title);
        link.setInlineEntity(entity);
        link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
        link.setRel(Constants.NS_ASSOCIATION_LINK_REL + title);
        return link;
    }

    public static URI createId(String entitySetName, Object id) {
        try {
            return new URI(entitySetName + "(" + String.valueOf(id) + ")");
        } catch (URISyntaxException e) {
            throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
        }
    }

    public static <T> T createInstance(final Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new ODataRuntimeException(ex);
        }
    }
}
