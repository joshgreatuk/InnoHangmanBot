package com.innocuous.jdamodulesystem;

import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;

public class JDAModuleBase
{
    public InteractionHook interactionHook;
    public CommandInteraction commandInteraction;
    public ComponentInteraction componentInteraction;

    public void BeforeExecute()
    {

    }

    public void AfterExecute()
    {

    }
}
