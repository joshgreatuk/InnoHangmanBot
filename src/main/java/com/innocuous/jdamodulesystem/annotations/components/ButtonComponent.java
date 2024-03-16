package com.innocuous.jdamodulesystem.annotations.components;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ButtonComponent
{
    public String customID();
    public boolean ignoreGroups() default false;
}
