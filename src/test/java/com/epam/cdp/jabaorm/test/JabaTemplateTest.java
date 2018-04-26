package com.epam.cdp.jabaorm.test;

import com.epam.cdp.jabaorm.JabaTemplate;
import com.epam.cdp.jabaorm.test.model.Car;
import com.epam.cdp.jabaorm.test.model.User;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

public class JabaTemplateTest {

    private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private static Connection connection = JabaTemplate.getDbConnection("org.h2.Driver", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "", "");

    static {
        if (connection == null) {
            throw new RuntimeException("Connection is not established");
        }

        try {
            String createUserTable = "CREATE TABLE user (user_id INT, name VARCHAR(255), birthdate DATE, is_man BOOLEAN);";
            PreparedStatement statement = connection.prepareStatement(createUserTable);
            statement.execute();
            statement.close();

            String createCarTable = "CREATE TABLE automobile (brand VARCHAR(255), manufacture_name DATE , power INT, colour VARCHAR(255));";
            statement = connection.prepareStatement(createCarTable);
            statement.execute();
            statement.close();
        } catch (Exception e) {
            System.out.println("Fail while creating tables");
        }
    }

    @Test
    public void userCrudTest() throws Exception {
        JabaTemplate<User> template = JabaTemplate.create(User.class, connection);

        // User to save
        User user = new User(1, "Bob", sdf.parse("31-07-1997"), true, null);

        template.save(user);

        PreparedStatement statement = connection.prepareStatement("select * from user");
        ResultSet resultSet = statement.executeQuery();

        // Check that user was saved
        while (resultSet.next()) {
            Assert.assertEquals(1, resultSet.getInt("user_id"));
            Assert.assertEquals("Bob", resultSet.getString("name"));
            Assert.assertEquals(Boolean.TRUE, resultSet.getBoolean("is_man"));
        }

        resultSet.close();

        // Search user by id
        User userToSearch = new User(1, null, null, null, null);
        User gotUser = template.search(userToSearch);

        // Check that user is got
        Assert.assertEquals(user, gotUser);

        // Update ID to the user
        User userWitchChangedId = new User(2, null, null, null, null);

        // Search user by id and update id
        template.update(userToSearch, userWitchChangedId);

        // Check that user was updated
        gotUser = template.search(userWitchChangedId);
        Assert.assertEquals(new Integer(2), gotUser.getUserId());

        // Delete user with updated id
        template.delete(gotUser);

        resultSet = statement.executeQuery();

        // Check that table is empty
        Assert.assertFalse(resultSet.next());
    }

    @Test
    public void carCrudTest() throws Exception {
        JabaTemplate<Car> template = JabaTemplate.create(Car.class, connection);

        Car car = new Car("BMW", sdf.parse("31-07-1997"), 249, "Sea blue");
        template.save(car);

        Car carFromDb = template.search(new Car("BMW", null, null, null));
        Assert.assertEquals(car, carFromDb);

        int updatedRows = template.update(carFromDb, new Car(null, null, 300, null));
        carFromDb = template.search(new Car("BMW", null, null, null));

        car.setPower(300);
        Assert.assertEquals(car, carFromDb);
        Assert.assertEquals(1, updatedRows);

        template.delete(car);
    }

}
