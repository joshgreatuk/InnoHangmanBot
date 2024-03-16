package com.innocuous.jdamodulesystem.data;

import com.innocuous.jdamodulesystem.annotations.SlashCommand;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.lang.reflect.Method;

public class SlashCommandDescriptor extends CommandDescriptor<SlashCommandData>
{
    public SlashCommandDescriptor(SlashCommandData data, Method commandMethod)
    {
        super(data, commandMethod);
    }
}
