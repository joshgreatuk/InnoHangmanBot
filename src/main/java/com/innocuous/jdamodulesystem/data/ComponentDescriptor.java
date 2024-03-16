package com.innocuous.jdamodulesystem.data;

import com.innocuous.jdamodulesystem.data.CommandDescriptor;
import net.dv8tion.jda.api.interactions.components.ActionComponent;

import java.lang.reflect.Method;

public class ComponentDescriptor
{
    public ComponentDescriptor(String customID, Boolean ignoreGroups, Method commandMethod)
    {
        this.customID = customID;
        this.ignoreGroups = ignoreGroups;
        this.commandMethod = commandMethod;
    }

    public String customID;
    public Boolean ignoreGroups;
    public Method commandMethod;
}
