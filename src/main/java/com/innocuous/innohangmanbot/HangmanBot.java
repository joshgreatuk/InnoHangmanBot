package com.innocuous.innohangmanbot;

import com.innocuous.dependencyinjection.*;
import com.innocuous.dependencyinjection.servicedata.IStoppable;
import com.innocuous.innohangmanbot.data.HangmanBotConfig;
import com.innocuous.innohangmanbot.services.*;
import com.innocuous.innohangmanbot.services.games.GameInstanceData;
import com.innocuous.innohangmanbot.services.games.GameInstanceService;
import com.innocuous.innohangmanbot.services.games.GameInstanceServiceConfig;
import com.innocuous.innohangmanbot.services.hangman.HangmanService;
import com.innocuous.innohangmanbot.services.hangman.HangmanServiceConfig;
import com.innocuous.innologger.*;
import com.innocuous.jdamodulesystem.InteractionService;
import com.innocuous.jdamodulesystem.data.InteractionConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;

import java.util.List;
import java.util.Optional;

public class HangmanBot
{
    public static void main(String[] args)
    {
        Optional<HangmanBot> botInstance = Optional.empty();
        try
        {
            botInstance = Optional.of(new HangmanBot());
            Optional<HangmanBot> finalBotInstance = botInstance;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> finalBotInstance.get().Shutdown()));
        }
        catch (Exception ex)
        {
            if (botInstance.isPresent())
            {
                botInstance.get().Shutdown();
            }
            System.out.println("HangmanBot caught exception:");
            System.out.println(ex);
            throw ex;
        }
    }

    private final IServiceProvider _services;
    private final HangmanBotConfig _config;
    private final InnoLoggerService _logger;

    public HangmanBot()
    {
        _services = BuildServiceCollection();
        _config = _services.GetService(HangmanBotConfig.class);
        _config.Init();
        _logger = _services.GetService(InnoLoggerService.class);
        JDABuilder client = _services.GetService(JDABuilder.class);
        client.setEnableShutdownHook(false);

        String botToken = _config.debugMode ? _config.debugBotToken : _config.releaseBotToken;
        client.setToken(botToken);

        try
        {
            //Add event listeners here
            client.setEventManager(new AnnotatedEventManager());
            List<Object> listenerServices = _services.<Object>GetServicesWithInterface(IJDAEventListener.class);
            listenerServices.add(_services.GetService(InteractionService.class));
            for (Object listener : listenerServices) { client.addEventListeners(listener); }

            client.setStatus(OnlineStatus.ONLINE);

            JDA instance = _services.GetService(JDA.class);
            Log(new LogMessage(this, "Bot running"));

            _services.GetService(HangmanService.class);

            // wait();
            // instance.awaitShutdown();
        }
        catch (Exception ex)
        {
            _logger.Log(new LogMessage(this, "A critical error occurred", LogSeverity.Critical, ex));
        }
    }

    public IServiceProvider BuildServiceCollection()
    {
        return new ServiceCollection()
                .AddSingletonService(HangmanBotConfig.class)
                .AddSingletonService(InnoLoggerConfig.class)
                .AddSingletonService(InteractionConfig.class)

                .AddSingletonService(InnoLoggerService.class)

                .AddSingletonService(JDABuilder.class, x -> JDABuilder.createDefault(""))
                .AddSingletonService(JDA.class, x -> x.<JDABuilder>GetService(JDABuilder.class).build())

                .AddSingletonService(InteractionService.class)

                .AddTransientService(InitializationService.class)
                //.AddTransientService(DataServiceInitializer.class)
                .AddTransientService(ModuleRegistrationService.class)

                .AddSingletonService(GameInstanceServiceConfig.class)
                .AddSingletonService(GameInstanceData.class)
                .AddSingletonService(GameInstanceService.class)

                .AddSingletonService(HangmanServiceConfig.class)
                .AddSingletonService(HangmanService.class)

                .Build();
    }

    public void Log(LogMessage message)
    { _logger.Log(message); }

    public void Shutdown()
    {
        _logger.Log(new LogMessage(this, "Starting shutdown"));

        //Shutdown services
        for (Object service : _services.GetActiveServices())
        {
            if (!IStoppable.class.isAssignableFrom(service.getClass())) continue;

            IStoppable stoppableService = (IStoppable)service;

            _logger.Log(new LogMessage(stoppableService, "Stopping service", LogSeverity.Info));

            try
            {
                stoppableService.Shutdown();
            }
            catch (Exception ex)
            {
                _logger.Log(new LogMessage(stoppableService, "Caught an exception during Shutdown", LogSeverity.Critical, ex));
            }
        }

        //Shutdown JDA
        _logger.Log(new LogMessage(this, "Shutting down JDA"));
        JDA instance = _services.<JDA>GetService(JDA.class);
        instance.shutdown();
        try
        {
            instance.awaitShutdown();
        }
        catch (Exception ex)
        {
            _logger.Log(new LogMessage(this, "JDA await shutdown failed, bot may not shutdown correctly!", LogSeverity.Critical, ex));
        }

        _logger.Log(new LogMessage(this, "Shutdown completed"));

        _services.<InnoLoggerService>GetService(InnoLoggerService.class).Shutdown();
    }
}
