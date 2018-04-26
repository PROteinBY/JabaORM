package com.epam.cdp.jabaorm.test.model;

import com.epam.cdp.jabaorm.annotations.JabaColumn;
import com.epam.cdp.jabaorm.annotations.JabaEntity;

import java.util.Date;
import java.util.Objects;

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

    public User() {
    }

    public User(Integer userId, String name, Date birthDate, Boolean isMan, String address) {
        this.userId = userId;
        this.name = name;
        this.birthDate = birthDate;
        this.isMan = isMan;
        this.address = address;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Boolean getMan() {
        return isMan;
    }

    public void setMan(Boolean man) {
        isMan = man;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) &&
                Objects.equals(name, user.name) &&
                Objects.equals(birthDate, user.birthDate) &&
                Objects.equals(isMan, user.isMan) &&
                Objects.equals(address, user.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, name, birthDate, isMan, address);
    }
}
