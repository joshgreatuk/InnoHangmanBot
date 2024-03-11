package com.innocuous.jdamodulesystem.data;

import java.util.ArrayList;

public class ModuleDescriptor
{
    public Class<?> moduleClass;
    public String groupName = "";
    public ArrayList<SlashCommandDescriptor> slashCommands = new ArrayList<>();

    public ModuleDescriptor(Class<?> moduleClass, String groupName)
    {
        this.moduleClass = moduleClass;
        this.groupName = groupName;
    }
}
