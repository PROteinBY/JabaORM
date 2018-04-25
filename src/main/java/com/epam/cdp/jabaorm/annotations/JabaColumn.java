package com.epam.cdp.jabaorm.annotations;


import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JabaColumn {

    String columnName() default "";

}
