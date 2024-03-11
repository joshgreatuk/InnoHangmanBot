package com.innocuous.jdamodulesystem.data;

import com.innocuous.jdamodulesystem.annotations.SlashCommand;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.lang.reflect.Method;

public class SlashCommandDescriptor
{
    public SlashCommandData data;
    public Method commandMethod;

    public SlashCommandDescriptor(SlashCommandData data, Method commandMethod)
    {
        this.data = data;
        this.commandMethod = commandMethod;
    }
}
