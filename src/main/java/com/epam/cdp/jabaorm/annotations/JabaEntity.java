package com.epam.cdp.jabaorm.annotations;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JabaEntity {

    String tableName() default "";

}
