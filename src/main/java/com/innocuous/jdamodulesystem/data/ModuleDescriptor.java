package com.innocuous.jdamodulesystem.data;

import java.util.Hashtable;

public class ModuleDescriptor
{
    public Class<?> moduleClass;

    public String subcommand = "";
    public String subcommandDesc = "";

    public String subcommandGroup = "";
    public String subcommandGroupDesc = "";

    public Hashtable<String, SlashCommandDescriptor> slashCommands = new Hashtable<>();
    public Hashtable<String, ComponentDescriptor> components = new Hashtable<>();

    public ModuleDescriptor(Class<?> moduleClass)
    {
        this.moduleClass = moduleClass;
    }
}
