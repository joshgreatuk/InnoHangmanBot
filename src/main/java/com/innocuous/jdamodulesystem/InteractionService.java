package com.innocuous.jdamodulesystem;

import com.innocuous.dependencyinjection.IServiceProvider;
import com.innocuous.innologger.ILogger;
import com.innocuous.innologger.InnoLoggerService;
import com.innocuous.innologger.LogMessage;
import com.innocuous.innologger.LogSeverity;
import com.innocuous.jdamodulesystem.annotations.Description;
import com.innocuous.jdamodulesystem.annotations.Group;
import com.innocuous.jdamodulesystem.annotations.SlashCommand;
import com.innocuous.jdamodulesystem.data.InteractionConfig;
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
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.apache.commons.collections4.Get;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public class InteractionService
{
    public InteractionService(ILogger logger, InteractionConfig config, JDABuilder jdaBuilder, IServiceProvider services)
    {
        _logger = logger;
        _config = (config == null ? new InteractionConfig() : config);
        _config.Init();
        _services = services;

        jdaBuilder.addEventListeners(this);
    }

    private final ILogger _logger;
    private final InteractionConfig _config;
    private final IServiceProvider _services;
    private JDA jda;

    private ArrayList<ModuleDescriptor> modules = new ArrayList<>();

    public void AddModules(Collection<Class<?>> targetClasses)
    {
        for (Class<?> moduleClass : targetClasses) { AddModule(moduleClass); }
    }

    public void AddModule(Class<?> targetClass)
    {
        AddModule(targetClass, "");
    }

    private void AddModule(Class<?> targetClass, String groupName)
    {
        List<String> groupsRaw = Arrays.stream(groupName.split("[ ]")).filter(x -> !x.isEmpty()).toList();
        ArrayList<String> groups = new ArrayList<>(groupsRaw);
        int groupNesting = groups.size();

        //If this class cant be assigned to a JDAModuleBase, skip it
        if (!JDAModuleBase.class.isAssignableFrom(targetClass))
        {
            _logger.Log(new LogMessage(this, "Class '" + targetClass.getName() + "' not assignable from JDAModuleBase", LogSeverity.Verbose));
            return;
        }

        //Check for class annotations (preconditions, groups)
        Group groupAnnotation = targetClass.getAnnotation(Group.class);
        String desc = "";
        if (groupAnnotation != null && groupNesting < 2)
        {
            groups.add(groupAnnotation.name().toLowerCase());
            desc = groupAnnotation.description();
        }

        ModuleDescriptor module = new ModuleDescriptor(targetClass);

        if (groups.size() >= 1) module.subcommand = groups.get(0);
        if (groups.size() == 1) module.subcommandDesc = desc;
        if (groups.size() >= 2)
        {
            module.subcommandGroup = groups.get(1);
            module.subcommandDesc = desc;
        }

        modules.add(module);

        //Check methods for annotations (preconditions, commands)
        for (Method classMethod : Arrays.stream(targetClass.getMethods())
                .filter(x -> x.isAnnotationPresent(SlashCommand.class)).toList())
        {
            //Check fields for annotations (type converters, autocompletes)
            SlashCommand slashCommand = classMethod.getAnnotation(SlashCommand.class);
            OptionData[] options = GetSlashCommandOptionData(classMethod);

            SlashCommandData commandData = Commands.slash(slashCommand.name().toLowerCase(), slashCommand.description());
            commandData.addOptions(options);

            module.slashCommands.put(((module.subcommand.isEmpty() ? "" : module.subcommand + " ")
                    + (module.subcommandGroup.isEmpty() ? "" : module.subcommandGroup + " ")
                    + commandData.getName()).trim(), new SlashCommandDescriptor(commandData, classMethod));
        }

        //Iterate through nested classes to AddModule
        for (Class<?> nestedClass : targetClass.getDeclaredClasses())
        {
            AddModule(nestedClass, String.join(" ", groups));
        }

        _logger.Log(new LogMessage(this, "Added module '" + targetClass.getName() + "'", LogSeverity.Debug));
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
        _logger.Log(new LogMessage(this, "Registered " + modules.size() + " modules globally"));
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

        if (guildID == 0)
        {
            guildID = _config.autoRegisterGuild;
            _logger.Log(new LogMessage(this, "RegisterModulesToGuild, GuildID was 0, using autoRegisterGuild"));
        }

        AddCommands(jda.getGuildById(guildID).updateCommands());
        _logger.Log(new LogMessage(this, "Registered " + modules.size() + " modules to guild"));
    }

    private void AddCommands(CommandListUpdateAction updateAction)
    {
        //Filter commands into no group (0 @Group), subcommands (1 @Group) and subgroup (2 @Group) subcommands
        Hashtable<String, SlashCommandData> rootSlashCommands = new Hashtable<>();
        Hashtable<String, SubcommandGroupData> subcommandGroups = new Hashtable<>();
        modules.forEach(x ->
                {
                    //Add slash commands to queue
                    if (!x.subcommand.isEmpty())
                    {
                        //Construct sub commands
                        ArrayList<SubcommandData> subCommands = new ArrayList<>();
                        for (SlashCommandDescriptor command : x.slashCommands.values())
                        {;
                            SubcommandData subCommand = new SubcommandData(command.data.getName(), command.data.getDescription());
                            subCommand.addOptions(command.data.getOptions());
                            subCommands.add(subCommand);
                        }

                        //There is atleast 1 @Group
                        SlashCommandData parentCommand = rootSlashCommands.get(x.subcommand);
                        if (parentCommand == null)
                        {
                            parentCommand = Commands.slash(x.subcommand, x.subcommandDesc);
                            rootSlashCommands.put(x.subcommand, parentCommand);
                        }

                        if (!x.subcommandGroup.isEmpty())
                        {
                            //Add SubcommandGroup
                            SubcommandGroupData groupData = subcommandGroups.get(x.subcommandGroup);
                            if (groupData == null)
                            {
                                groupData = new SubcommandGroupData(x.subcommandGroup, x.subcommandGroupDesc);
                                subcommandGroups.put(x.subcommandGroup, groupData);
                                parentCommand.addSubcommandGroups(groupData);
                            }

                            groupData.addSubcommands(subCommands);
                        }
                        else
                        {
                            parentCommand.addSubcommands(subCommands);
                        }
                    }
                    else
                    {
                        for (SlashCommandDescriptor command : x.slashCommands.values())
                        {
                            rootSlashCommands.put(command.data.getName(), command.data);
                        }
                    }
                }
        );

        //Add commands to queue and register
        updateAction.addCommands(rootSlashCommands.values()).queue();
    }

    @SubscribeEvent
    public void onReady(ReadyEvent event)
    {
        jda = event.getJDA();

        if (_config.commandAutoRegister)
        {
            if (_config.autoRegisterGlobally)
            {
                RegisterModulesGlobally();
            }
            else if (_config.autoRegisterGuild != 0)
            {
                RegisterModulesToGuild(_config.autoRegisterGuild);
            }
            else
            {
                _logger.Log(new LogMessage(this, "autoRegisterGuild set to 0!", LogSeverity.Warning));
            }
        }
    }

    public void setJDA(JDA jda)
    {
        this.jda = jda;
    }

    @SubscribeEvent
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        //Find better way to traverse modules
        String command = event.getFullCommandName();
        Optional<ModuleDescriptor> targetModule = modules.stream().findFirst().filter(x -> x.slashCommands.containsKey(command));
        if (targetModule.isEmpty())
        {
            throw new NullPointerException();
        }

        JDAModuleBase module = this.<JDAModuleBase>InstantiateModule(targetModule.get().moduleClass);
        module.interaction = event.getInteraction();

        SlashCommandDescriptor commandDescriptor = targetModule.get().slashCommands.get(command);

        //Get parameters (remember to handle Optionals)
        Object[] optionParams = new Object[commandDescriptor.commandMethod.getParameterCount()];
        Parameter[] params = commandDescriptor.commandMethod.getParameters();
        for (int i=0; i < commandDescriptor.commandMethod.getParameterCount(); i++)
        {
            Parameter param = params[i];
            OptionMapping option = module.interaction.getOption(param.getName());

            if (param.getType() == Optional.class)
            {

                Object optionObject = GetOptionToObject(option);
                if (optionObject == null) optionParams[i] = Optional.empty();
                else optionParams[i] = Optional.of(optionObject);
            }

            optionParams[i] = GetOptionToObject(option);
        }

        //Invoke module
        try
        {
            commandDescriptor.commandMethod.invoke(module, optionParams);
        }
        catch (Exception ex)
        {
            _logger.Log(new LogMessage(this, "Module '" + module.getClass().getName() + "' threw exception", LogSeverity.Error, ex));
            module.interaction.reply("An error occured").setEphemeral(true);
        }
    }

    private Object GetOptionToObject(OptionMapping option)
    {
        return switch (option.getType())
        {
            case STRING -> option.getAsString();
            case INTEGER -> option.getAsInt();
            case BOOLEAN -> option.getAsBoolean();
            case USER -> option.getAsUser();
            case CHANNEL -> option.getAsChannel();
            case ROLE -> option.getAsRole();
            case NUMBER -> option.getAsDouble();
            case ATTACHMENT -> option.getAsAttachment();
            default -> null;
        };
    }

    @SubscribeEvent
    public void onMessageReceived(MessageReceivedEvent event) {
    }

    @SubscribeEvent
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
    }

    @SubscribeEvent
    public void onUserContextInteraction(UserContextInteractionEvent event) {
    }

    @SubscribeEvent
    public void onButtonInteraction(ButtonInteractionEvent event) {
    }

    @SubscribeEvent
    public void onEntitySelectInteraction(EntitySelectInteractionEvent event) {
    }

    @SubscribeEvent
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
    }

    @SubscribeEvent
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
    }

    private <T> T InstantiateModule(Class<?> moduleClass)
    {
        Constructor[] constructors = moduleClass.getDeclaredConstructors();

        //Take the first constructor
        Constructor constructor = constructors[0];
        _logger.Log(new LogMessage(this, "Using constructor '" + constructor.toString() + "'", LogSeverity.Debug));
        Object[] params = new Object[constructor.getParameterCount()];
        Class<?>[] paramTypes = constructor.getParameterTypes();

        for (int i=0; i < paramTypes.length; i++)
        {
            params[i] = _services.GetService(paramTypes[i]);
            _logger.Log(new LogMessage(this, "Param type '" + paramTypes[i].getName() + "' for '" + moduleClass.getName() + "' value is '" + (params[i] == null ? "null" : params[i].toString()) + "'", LogSeverity.Verbose));
            if (params[i] == null) _logger.Log(new LogMessage(this, "Param of type '" + paramTypes[i].getName() + "' for '" + moduleClass.getName() + "'s constructor value is null", LogSeverity.Warning));
        }

        try
        {
            _logger.Log(new LogMessage(this, "Instantiated module '" + moduleClass.getName() + "' with '" + params.length + "' params", LogSeverity.Debug));
            return (T)constructor.newInstance(params);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
