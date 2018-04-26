package com.epam.cdp.jabaorm.test.model;

import com.epam.cdp.jabaorm.annotations.JabaColumn;
import com.epam.cdp.jabaorm.annotations.JabaEntity;

import java.util.Date;
import java.util.Objects;

@JabaEntity(tableName = "automobile")
public class Car {

    @JabaColumn
    private String brand;

    @JabaColumn(columnName = "manufacture_name")
    private Date manufactureYear;

    @JabaColumn
    private Integer power;

    @JabaColumn
    private String colour;

    public Car() {
    }

    public Car(String brand, Date manufactureYear, Integer power, String colour) {
        this.brand = brand;
        this.manufactureYear = manufactureYear;
        this.power = power;
        this.colour = colour;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Date getManufactureYear() {
        return manufactureYear;
    }

    public void setManufactureYear(Date manufactureYear) {
        this.manufactureYear = manufactureYear;
    }

    public Integer getPower() {
        return power;
    }

    public void setPower(Integer power) {
        this.power = power;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return Objects.equals(brand, car.brand) &&
                Objects.equals(manufactureYear, car.manufactureYear) &&
                Objects.equals(power, car.power) &&
                Objects.equals(colour, car.colour);
    }

    @Override
    public int hashCode() {

        return Objects.hash(brand, manufactureYear, power, colour);
    }
}
