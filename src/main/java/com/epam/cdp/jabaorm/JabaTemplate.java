package com.epam.cdp.jabaorm;

import com.epam.cdp.jabaorm.annotations.JabaColumn;
import com.epam.cdp.jabaorm.annotations.JabaEntity;
import com.sun.rowset.JdbcRowSetImpl;
import javafx.util.Pair;
import org.apache.commons.text.StringSubstitutor;

import javax.sql.rowset.JdbcRowSet;
import java.lang.reflect.Field;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
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
    private Connection dbConnection;

    private String insertTemplate;
    private String updateTemplate;
    private String deleteTemplate;
    private String selectTemplate;

    // Prepare template for target class. Scan fields and types, create mappings and templates.
    public static <E> JabaTemplate<E> create(Class<E> targetClass, Connection connection) throws Exception {
        JabaTemplate<E> template = new JabaTemplate<>(targetClass, connection);

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

        template.updateTemplate =
                "UPDATE " + template.targetTableName +
                        " SET ${updatedValues} WHERE ${conditions};";

        template.deleteTemplate =
                "DELETE FROM " + template.targetTableName +
                        " WHERE ${conditions};";


        return template;
    }

    private JabaTemplate(Class<T> targetClass, Connection connection) throws SQLException {
        this.targetClass = targetClass;
        this.dbConnection = connection;
    }

    /**
     * Return the first object that match the criteria
     *
     * @param searchObject object with criteria (null to ignore field)
     * @return first object that match the criteria
     * @throws Exception any errors
     */
    public T search(T searchObject) throws Exception {
        String selectQuery = getSelectQuery(searchObject);

        JdbcRowSet rowSet = new JdbcRowSetImpl(dbConnection);
        rowSet.setType(ResultSet.TYPE_SCROLL_INSENSITIVE);

        rowSet.setCommand(selectQuery);
        rowSet.execute();

        if (!rowSet.next()) {
            return null;
        }

        Object object = targetClass.newInstance();

        for (String fieldName : columnMappings.keySet()) {
            Field field = object.getClass().getDeclaredField(fieldName);

            field.setAccessible(true);
            field.set(object, rowSet.getObject(columnMappings.get(fieldName)));
        }

        return targetClass.cast(object);
    }

    // Just save object as a new row
    public void save(T object) throws Exception {
        dbConnection
                .prepareStatement(getInsertQuery(object))
                .execute();
    }

    // Update object with set fields. Return number of updated rows.
    public int update(T searchObject, T updatedObject) throws Exception {
        return dbConnection
                .prepareStatement(getUpdateQuery(searchObject, updatedObject))
                .executeUpdate();
    }

    /**
     * Delete all matched rows
     * @param searchObject object with criteria
     * @throws Exception any errors
     */
    public void delete(T searchObject) throws Exception {
        String deleteQuery = getDeleteQuery(searchObject);

        PreparedStatement statement = dbConnection.prepareStatement(deleteQuery);
        statement.execute();
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

    private String getUpdateQuery(T searchObject, T updatedObject) throws Exception {
        Map<String, String> gaps = new HashMap<>(2);
        List<Pair<String, String>> fieldsNameValues = getFieldsNameValueList(searchObject);
        List<Pair<String, String>> fieldsNameValuesForUpdatedObject = getFieldsNameValueList(updatedObject);

        gaps.put("updatedValues", String.join(
                ", ",
                fieldsNameValuesForUpdatedObject
                        .stream()
                        .map(fv -> fv.getKey() + " = " + fv.getValue())
                        .collect(Collectors.toList())
        ));

        gaps.put("conditions", String.join(
                " AND ",
                fieldsNameValues
                        .stream()
                        .map(fv -> fv.getKey() + " = " + fv.getValue())
                        .collect(Collectors.toList())
        ));

        StringSubstitutor substitutor = new StringSubstitutor(gaps);
        return substitutor.replace(updateTemplate);
    }

    private String getDeleteQuery(T searchObject) throws Exception {
        Map<String, String> gaps = new HashMap<>(2);
        List<Pair<String, String>> fieldsNameValues = getFieldsNameValueList(searchObject);

        gaps.put("conditions", String.join(
                " AND ",
                fieldsNameValues
                        .stream()
                        .map(fv -> fv.getKey() + " = " + fv.getValue())
                        .collect(Collectors.toList())
        ));

        StringSubstitutor substitutor = new StringSubstitutor(gaps);
        return substitutor.replace(deleteTemplate);
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
            return "'" + object.toString() + "'";
        }

        if (object instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return "'" + sdf.format(object) + "'";
        }

        return object.toString();
    }

    /**
     * Create Connection object
     * @param driver JDBC driver
     * @param connection url to connect
     * @param user database user
     * @param pass user's password
     * @return Connection object
     */
    public static Connection getDbConnection(String driver, String connection, String user, String pass) {
        Connection dbConnection = null;

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try {
            dbConnection = DriverManager.getConnection(connection, user, pass);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return dbConnection;
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
