package com.innocuous.innohangmanbot;

import com.innocuous.dependencyinjection.*;
import com.innocuous.dependencyinjection.servicedata.IStoppable;
import com.innocuous.innohangmanbot.services.ErrorThrower;
import com.innocuous.innohangmanbot.services.IJDAEventListener;
import com.innocuous.innohangmanbot.services.InitializationService;
import com.innocuous.innohangmanbot.services.InnoLoggerDIBridge;
import com.innocuous.innologger.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import org.apache.commons.collections4.Get;

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

        String botToken = _config.debugMode ? _config.debugBotToken : _config.releaseBotToken;
        client.setToken(botToken);

        try
        {
            //Add event listeners here
            client.setEventManager(new AnnotatedEventManager());
            List<Object> listenerServices = _services.<Object>GetServicesWithInterface(IJDAEventListener.class);
            for (Object listener : listenerServices) { client.addEventListeners(listener); }

            client.setStatus(OnlineStatus.ONLINE);

            JDA instance = _services.GetService(JDA.class);
            Log(new LogMessage(this, "Bot running"));

            // wait();
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

                .AddSingletonService(InnoLoggerService.class)
                .AddSingletonService(InnoLoggerDIBridge.class)

                .AddSingletonService(JDABuilder.class, x -> JDABuilder.createDefault(""))
                .AddSingletonService(JDA.class, x -> x.<JDABuilder>GetService(JDABuilder.class).build())

                .AddSingletonService(InitializationService.class)

                .AddSingletonService(ErrorThrower.class)

                .AddLogConsumer(InnoLoggerDIBridge.class, "Log")
                .Build();
    }

    public void Log(LogMessage message)
    { _logger.Log(message); }

    public void Shutdown()
    {
        _logger.Log(new LogMessage(this, "Starting shutdown"));

        //Shutdown JDA
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

        _logger.Log(new LogMessage(this, "Shutdown completed"));

        _services.<InnoLoggerService>GetService(InnoLoggerService.class).Shutdown();
    }
}
