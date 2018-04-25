package com.epam.cdp.jabaorm.test.model;

import com.epam.cdp.jabaorm.annotations.JabaColumn;
import com.epam.cdp.jabaorm.annotations.JabaEntity;

import java.util.Date;

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

    public User(Integer userId, String name, Date birthDate, Boolean isMan, String address) {
        this.userId = userId;
        this.name = name;
        this.birthDate = birthDate;
        this.isMan = isMan;
        this.address = address;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", birthDate=" + birthDate +
                ", isMan=" + isMan +
                ", address='" + address + '\'' +
                '}';
    }
}
