package com.bloggingit.odata.storage;

import com.bloggingit.odata.model.BaseEntity;
import com.bloggingit.odata.model.Book;
import com.bloggingit.odata.exception.EntityDataException;
import com.bloggingit.odata.model.Author;
import com.bloggingit.odata.model.Gender;
import com.bloggingit.odata.olingo.v4.util.ReflectionUtils;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class provides a simple in-memory data storage with some example data.
 */
public class InMemoryDataStorage {

    private static final ConcurrentMap<Long, BaseEntity> DATA_BOOKS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Long, BaseEntity> DATA_AUTHOR = new ConcurrentHashMap<>();

    static {
        LocalDateTime lDate1 = LocalDateTime.of(2011, Month.JULY, 21, 0, 0);
        LocalDateTime lDate2 = LocalDateTime.of(2015, Month.AUGUST, 6, 13, 15);
        LocalDateTime lDate3 = LocalDateTime.of(2013, Month.MAY, 12, 0, 0);

        Date date1 = Date.from(lDate1.atZone(ZoneId.systemDefault()).toInstant());
        Date date2 = Date.from(lDate2.atZone(ZoneId.systemDefault()).toInstant());
        Date date3 = Date.from(lDate3.atZone(ZoneId.systemDefault()).toInstant());

        Book book1 = new Book("Book Title 1", "This is the description of book 1", date1, "Author 1", 9.95, true);
        Book book2 = new Book("Book Title 2", "This is the description of book 2", date2, "Author 2", 5.99, true);
        Book book3 = new Book("Book Title 3", "This is the description of book 3", date3, "Author 3", 14.50, false);

        book1.setId(1L);
        book2.setId(2L);
        book3.setId(3L);

        DATA_BOOKS.put(book1.getId(), book1);
        DATA_BOOKS.put(book2.getId(), book2);
        DATA_BOOKS.put(book3.getId(), book3);

        Author author1 = new Author("Author 1", Gender.MALE);
        Author author2 = new Author("Author 2", Gender.FEMALE);
        Author author3 = new Author("Author 3", Gender.UNKOWN);

        author1.setId(1L);
        author2.setId(2L);
        author3.setId(3L);

        DATA_AUTHOR.put(author1.getId(), author1);
        DATA_AUTHOR.put(author2.getId(), author2);
        DATA_AUTHOR.put(author3.getId(), author3);
    }

    @SuppressWarnings("unchecked")
    private static <T> ConcurrentMap<Long, T> getDataMapByEntityClass(Class<T> entityClazz) {
        ConcurrentMap<Long, T> entities = null;

        if (Book.class.equals(entityClazz)) {
            entities = (ConcurrentMap<Long, T>) DATA_BOOKS;
        } else if (Author.class.equals(entityClazz)) {
            entities = (ConcurrentMap<Long, T>) DATA_AUTHOR;
        }

        return entities;
    }

    public static <T> List<T> getDataListByBaseEntityClass(Class<T> entityClazz) {
        final ConcurrentMap<Long, T> entityMap = getDataMapByEntityClass(entityClazz);

        return new ArrayList<>(entityMap.values());
    }

    public static <T> T getDataByClassAndId(Class<T> entityClazz, long id) {
        final ConcurrentMap<Long, T> entityMap = getDataMapByEntityClass(entityClazz);

        return entityMap.get(id);
    }

    public static <T> void deleteDataByClassAndId(Class<T> entityClazz, long id) {
        final ConcurrentMap<Long, T> entityMap = getDataMapByEntityClass(entityClazz);
        entityMap.remove(id);
    }

    public static <T> T createEntity(T newEntity) throws EntityDataException {

        if (newEntity == null) {
            throw new EntityDataException("Unable to create entity, because no entity given");
        }

        if (newEntity instanceof BaseEntity) {
            @SuppressWarnings("unchecked")
            final ConcurrentMap<Long, T> entityMap = getDataMapByEntityClass((Class<T>) newEntity.getClass());

            BaseEntity baseEntity = (BaseEntity) newEntity;

            if (baseEntity.getId() == 0 || entityMap.putIfAbsent(baseEntity.getId(), newEntity) == null) {
                baseEntity.setId(entityMap.size() + 1);
                entityMap.put(baseEntity.getId(), newEntity);
            } else {
                throw new EntityDataException("Could not create entity, because it already exists");
            }
        } else {
            throw new EntityDataException("Unable to create unsupported entity class" + newEntity);
        }

        return newEntity;
    }

    @SuppressWarnings("unchecked")
    public static <T> T updateEntity(Class<T> entityClazz, long id, Map<String, Object> newPropertyValues, boolean nullableUnkownProperties) throws EntityDataException {
        T updatedEntity = null;

        if (BaseEntity.class.isAssignableFrom(entityClazz)) {
            final ConcurrentMap<Long, BaseEntity> entityMap = (ConcurrentMap<Long, BaseEntity>) getDataMapByEntityClass(entityClazz);

            BaseEntity baseEntity = entityMap.get(id);

            if (baseEntity == null) {
                throw new EntityDataException("Unable to update entity, because the entity does not exist");
            }

            newPropertyValues.entrySet().forEach((propEntry) -> {
                String fieldname = propEntry.getKey();
                Object value = propEntry.getValue();
                if (!("id".equalsIgnoreCase(fieldname))) {
                    ReflectionUtils.invokePropertySetter(fieldname, baseEntity, value);
                }
            });

            entityMap.put(baseEntity.getId(), baseEntity);

            updatedEntity = (T) baseEntity;
        } else {
            throw new EntityDataException("Unable to update unsupported entity class" + entityClazz);
        }

        return updatedEntity;
    }
}
