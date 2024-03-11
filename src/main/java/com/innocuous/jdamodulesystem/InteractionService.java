package com.innocuous.jdamodulesystem;

import com.innocuous.innologger.ILogger;
import com.innocuous.innologger.LogMessage;
import com.innocuous.innologger.LogSeverity;
import com.innocuous.jdamodulesystem.annotations.Description;
import com.innocuous.jdamodulesystem.annotations.Group;
import com.innocuous.jdamodulesystem.annotations.SlashCommand;
import com.innocuous.jdamodulesystem.data.ModuleDescriptor;
import com.innocuous.jdamodulesystem.data.SlashCommandDescriptor;
import net.dv8tion.jda.api.JDA;
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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class InteractionService extends ListenerAdapter
{
    public InteractionService(ILogger logger, JDABuilder jdaBuilder)
    {
        _logger = logger;

        jdaBuilder.addEventListeners(this);
    }

    private final ILogger _logger;
    private JDA jda;

    private ArrayList<ModuleDescriptor> modules = new ArrayList<>();

    public void AddModule(Class<?> targetClass)
    {
        AddModule(targetClass, "");
    }

    private void AddModule(Class<?> targetClass, String groupName)
    {
        //If this class cant be assigned to a JDAModuleBase, skip it
        if (!JDAModuleBase.class.isAssignableFrom(targetClass))
        {
            _logger.Log(new LogMessage(this, "Class '" + targetClass.getName() + "' not assignable from JDAModuleBase", LogSeverity.Verbose));
            return;
        }

        //Check for class annotations (preconditions, groups)
        Group groupAnnotation = targetClass.getAnnotation(Group.class);
        if (groupAnnotation != null)
        {
            groupName += " " + groupAnnotation.name();
            groupName = groupName.trim();
        }

        ModuleDescriptor module = new ModuleDescriptor(targetClass, groupName);
        modules.add(module);

        //Check methods for annotations (preconditions, commands)
        for (Method classMethod : Arrays.stream(targetClass.getMethods())
                .filter(x -> x.isAnnotationPresent(SlashCommand.class)).toList())
        {
            //Check fields for annotations (type converters, autocompletes)
            SlashCommand slashCommand = classMethod.getAnnotation(SlashCommand.class);
            OptionData[] options = GetSlashCommandOptionData(classMethod);

            SlashCommandData commandData = Commands.slash(slashCommand.name(), slashCommand.description());
            commandData.addOptions(options);

            module.slashCommands.add(new SlashCommandDescriptor(commandData, classMethod));
        }

        //Iterate through nested classes to AddModule
        for (Class<?> nestedClass : targetClass.getDeclaredClasses())
        {
            AddModule(nestedClass, groupName);
        }
    }

    private OptionData[] GetSlashCommandOptionData(Method targetMethod)
    {
        OptionData[] options = new OptionData[targetMethod.getParameterCount()];
        Parameter[] params = targetMethod.getParameters();
        for (int i = 0; i < params.length; i++)
        {
            Parameter param = params[i];
            String paramTypeName = param.getType().getSimpleName();
            boolean required = true;
            if (param.getType() == Optional.class)
            {
                String[] paramStrings = param.getAnnotatedType().toString().split("[<>]")[1].split("[.]");
                paramTypeName = paramStrings[paramStrings.length-1];
                required = false;
            }

            OptionType type = switch (paramTypeName)
            {
                case "String" -> OptionType.STRING;
                case "Integer" -> OptionType.INTEGER;
                case "Boolean" -> OptionType.BOOLEAN;
                case "User" -> OptionType.USER;
                case "Channel" -> OptionType.CHANNEL;
                case "Role" -> OptionType.ROLE;
                case "IMentionable" -> OptionType.MENTIONABLE;
                case "Double" -> OptionType.NUMBER;
                case "Attachment" -> OptionType.ATTACHMENT;
                default -> OptionType.UNKNOWN;
            };

            String description = "A " + type.name() + " parameter";
            Description descAnnotation = param.getAnnotation(Description.class);
            if (descAnnotation != null) description = descAnnotation.description();

            if (type == OptionType.UNKNOWN) throw new UnknownError("Slash command parameter is of unknown type");

            options[i] = new OptionData(type, param.getName(), description, required);
        }

        return options;
    }

    public void RegisterModulesGlobally()
    {
        //Register commands to discord, Ready must have been called
        if (jda == null)
        {
            _logger.Log(new LogMessage(this,
                    "JDA has not fired Ready event yet, can't register commands",
                    LogSeverity.Warning));
            return;
        }

        AddCommands(jda.updateCommands());
    }

    public void RegisterModulesToGuild(Long guildID)
    {
        //Register commands to discord, Ready must have been called
        if (jda == null)
        {
            _logger.Log(new LogMessage(this,
                    "JDA has not fired Ready event yet, can't register commands",
                    LogSeverity.Warning));
            return;
        }

        AddCommands(jda.getGuildById(guildID).updateCommands());
    }

    private void AddCommands(CommandListUpdateAction updateAction)
    {
        //Filter commands into no group (0 @Group), subcommands (1 @Group) and subgroup (2 @Group) subcommands


        //Add commands


        updateAction.queue();
    }

    @Override
    public void onReady(ReadyEvent event)
    {
        jda = event.getJDA();
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
