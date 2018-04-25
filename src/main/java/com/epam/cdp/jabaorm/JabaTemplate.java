package com.epam.cdp.jabaorm;

import com.epam.cdp.jabaorm.annotations.JabaColumn;
import com.epam.cdp.jabaorm.annotations.JabaEntity;
import javafx.util.Pair;
import org.apache.commons.text.StringSubstitutor;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The best ORM solution.
 *
 * Support only some basic types (see supportedTypes array).
 * User create(Class) method to construct new instance.
 *
 * User @JabaEntity and @JabaColumn to mark you classes.
 *
 * searchObject is an object with set fields that are used to create filters.
 * null fields are ignored while creating where A=B constructions
 *
 * @param <T> type of JabaEntity
 */
public class JabaTemplate<T> {

    private String targetTableName;
    private Map<String, String> columnMappings;
    private Class<T> targetClass;

    private String insertTemplate;
    private String updateTemplate;
    private String deleteTemplate;
    private String selectTemplate;

    public static <E> JabaTemplate<E> create(Class<E> targetClass) {
        JabaTemplate<E> template = new JabaTemplate<>(targetClass);

        JabaEntity entityMark = targetClass.getDeclaredAnnotation(JabaEntity.class);

        if (entityMark != null) {
            if (!entityMark.tableName().isEmpty()) {
                template.targetTableName = entityMark.tableName();
            } else {
                template.targetTableName = targetClass.getSimpleName().toLowerCase();
            }
        } else {
            throw new RuntimeException("Class " + targetClass.getName() + " is not marked as JabaEntity");
        }

        Field[] targetClassFields = targetClass.getDeclaredFields();
        template.columnMappings = new HashMap<>(targetClassFields.length);

        for (Field field : targetClassFields) {
            JabaColumn jabaColumn = field.getAnnotation(JabaColumn.class);

            if (jabaColumn != null) {
                if (!isTypeSupported(field.getType())) {
                    throw new RuntimeException(
                            "Type " + field.getType().getName() +
                                    " is not supported (JabaEntity: " + template.targetTableName + ")"
                    );
                }

                String fieldMapping = "";

                if (jabaColumn.columnName().isEmpty()) {
                    fieldMapping = field.getName().toLowerCase();
                } else {
                    fieldMapping = jabaColumn.columnName();
                }

                if (template.columnMappings.values().contains(fieldMapping)) {
                    throw new RuntimeException(
                            "Column '" + fieldMapping + "' is declared at least twice in " + template.targetTableName
                    );
                }

                template.columnMappings.put(field.getName(), fieldMapping);
            }
        }

        template.insertTemplate =
                "INSERT INTO " +
                        template.targetTableName +
                        " (${fieldsToInsert}) VALUES (${valuesToInsert});";

        template.selectTemplate =
                "SELECT * FROM " +
                        template.targetTableName +
                        " WHERE ${conditions};";


        return template;
    }

    private JabaTemplate(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    public T search(T searchObject) throws Exception {
        String selectQuery = getSelectQuery(searchObject);
        System.out.println(selectQuery);
        return null;
    }

    public T save(T object) throws Exception {
        String insertString = getInsertQuery(object);
        System.out.println(insertString);
        return null;
    }

    public T update(T searchObject, T updatedObject) {
        return null;
    }

    public void delete(T searchObject) {
    }

    private String getInsertQuery(T object) throws Exception {
        Map<String, String> gaps = new HashMap<>(2);
        List<Pair<String, String>> fieldsNameValues = getFieldsNameValueList(object);

        gaps.put("fieldsToInsert", String.join(
                ", ",
                fieldsNameValues
                        .stream()
                        .map(Pair::getKey)
                        .collect(Collectors.toList())
        ));

        gaps.put("valuesToInsert", String.join(
                ", ",
                fieldsNameValues
                        .stream()
                        .map(Pair::getValue)
                        .collect(Collectors.toList())
        ));

        StringSubstitutor substitutor = new StringSubstitutor(gaps);
        return substitutor.replace(insertTemplate);
    }

    private String getSelectQuery(T searchObject) throws Exception {
        Map<String, String> gaps = new HashMap<>(1);
        List<Pair<String, String>> fieldsNameValues = getFieldsNameValueList(searchObject);

        gaps.put("conditions", String.join(
                " AND ",
                fieldsNameValues
                        .stream()
                        .map(fv -> fv.getKey() + " = " + fv.getValue())
                        .collect(Collectors.toList())
        ));

        StringSubstitutor substitutor = new StringSubstitutor(gaps);
        return substitutor.replace(selectTemplate);
    }

    // Return list of fieldName -> fieldValue for sql
    private List<Pair<String, String>> getFieldsNameValueList(T object) throws Exception {
        List<Pair<String, String>> fieldsValuesList = new ArrayList<>();

        for (String fieldName : columnMappings.keySet()) {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);

            Object fieldValue = field.get(object);

            if (fieldValue != null) {
                fieldsValuesList.add(new Pair<>(columnMappings.get(fieldName), getSqlFriendlyValueString(fieldValue)));
            }
        }

        return fieldsValuesList;
    }

    /**
     * Some non-trivial converting can be placed here
     * @param object object to convert to string
     * @return converted string
     */
    private String getSqlFriendlyValueString(Object object) {
        if (object instanceof String) {
            return "'" + object + "'";
        }

        if (object instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            return "'" + sdf.format(object) + "'";
        }

        return object.toString();
    }

    /**
     * Check that type of the field is supported
     * @param type type to check
     * @return check result
     */
    private static boolean isTypeSupported(Class<?> type) {
        for (Class<?> supportedType : supportedTypes) {
            if (type.equals(supportedType)) {
                return true;
            }
        }

        return false;
    }

    // List of supported types
    private static Class<?>[] supportedTypes = {
            Long.class,
            Integer.class,
            Short.class,
            Float.class,
            Double.class,
            String.class,
            Date.class,
            Boolean.class,
            long.class,
            int.class,
            short.class,
            float.class,
            double.class,
            boolean.class
    };

}
