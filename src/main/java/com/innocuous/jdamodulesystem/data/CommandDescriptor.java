package com.innocuous.jdamodulesystem.data;

import net.dv8tion.jda.api.interactions.commands.Command;

import java.lang.reflect.Method;

public abstract class CommandDescriptor <DataType>
{
    public DataType data;
    public Method commandMethod;

    public CommandDescriptor(DataType data, Method method)
    {
        this.data = data;
        this.commandMethod = method;
    }
}
