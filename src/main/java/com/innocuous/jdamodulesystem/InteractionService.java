package com.innocuous.jdamodulesystem;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;

public class InteractionService extends ListenerAdapter
{
    public InteractionService(JDABuilder jdaBuilder)
    {
        jdaBuilder.addEventListeners(this);
    }

    public void AddPackageModules(String javaPackage)
    {
        //Find classes assignable to type JDAModuleBase
        //Recursively check internal classes for more annotations
        //Check those class methods for command annotations
        //Create ModuleDescriptor with commands registered
        //Take note of group, add group name to commands, chain groups
        Package targetPackage = ClassLoader.getSystemClassLoader().getDefinedPackage(javaPackage);

        if (targetPackage == null)
        {

        }
    }

    @Override
    public void onReady(ReadyEvent event)
    {
        //Register commands with discord
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);
    }

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        super.onMessageContextInteraction(event);
    }

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        super.onUserContextInteraction(event);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        super.onButtonInteraction(event);
    }

    @Override
    public void onEntitySelectInteraction(EntitySelectInteractionEvent event) {
        super.onEntitySelectInteraction(event);
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        super.onStringSelectInteraction(event);
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        super.onCommandAutoCompleteInteraction(event);
    }
}
