# JabaORM

The best ORM soultion.  

How to use?
* Mark your class with ```@JabaEntity``` annotation;
* Mark fields with ```@JabaColumn``` annotation;
* Create connection (you can use ```JabaTemplate.getDbConnection```);
* Create JabaTemplate for your entity like ```JabaTemplate.create(User.class, connection)```;

Example of JabaEntity

~~~

@JabaEntity(tableName = "user")
public class User {

    @JabaColumn(columnName = "user_id")
    private Integer userId;

    @JabaColumn
    private String name;

    @JabaColumn(columnName = "birthdate")
    private Date birthDate;

    @JabaColumn(columnName = "is_man")
    private Boolean isMan;

    // Just ignored field
    private String address;

    // Required constructor
    public User() {
    }
}

~~~

Make sure:
* Jaba entity must have an empty constructor (because ORM uses ```Class<T>.newInstance()``` method to create Java objects);
* You can mark only standard types with ```@JabaColumn``` (see supported types below);
* Yoy haven't to create two or more ```JabaColumn``` with equals ```columnName```;
* Tables for entities have to be prepared (ORM can't create it for you);

Supported types:
* String
* Short
* Integer
* Long
* Float
* Double
* Date
* Boolean
* primitive types

Usage: 
* ```JabaTemplate.search(searchObject)``` takes object to create filters. All null fields are ignored;  
Example:
~~~
JabaTemplate<Car> template = JabaTemplate.create(Car.class, connection);
// Get first "BMW" car
Car car = template.search(new Car("BMW", null, null, null));
~~~

* ```JabaTemplate.save(object)``` just save a given object;
* ```JabaTemplate.delete(searchObject)``` deletes all objects that matches given filters (equals to not-null fields);  
Example:
~~~
// Delete all "BMW" cars
template.delete(new Car("BMW", null, null, null));
~~~
* ```JabaTemplate.update(searchObject, updatedObject)``` updates all matched (filters are created by not-null searchObject fields) rows with not-null values from updatedObject;  

For more examples see tests  
You can buy enterprise support. Just like this repo and I'll contact you.