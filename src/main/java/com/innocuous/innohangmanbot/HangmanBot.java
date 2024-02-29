package com.innocuous.innohangmanbot;

import com.innocuous.dependencyinjection.*;
import com.innocuous.dependencyinjection.servicedata.IStoppable;
import com.innocuous.innohangmanbot.services.InitializationService;
import com.innocuous.innologger.*;
import net.dv8tion.jda.api.JDABuilder;

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
    private final JDABuilder _client;

    public HangmanBot()
    {
        _services = BuildServiceCollection();
        _config = _services.GetService(HangmanBotConfig.class);
        _config.Init();
        _logger = _services.GetService(InnoLoggerService.class);
        _client = _services.GetService(JDABuilder.class);

        String botToken = _config.debugMode ? _config.debugBotToken : _config.releaseBotToken;
        _client.setToken(botToken);

        try
        {
            _client.build();

            //Initialize services here
            _client.addEventListeners(_services.GetService(InitializationService.class));

            wait();
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
                .AddSingletonService(JDABuilder.class, x -> JDABuilder.createDefault(""))

                .Build();
    }

    public void Log(LogMessage message)
    { _logger.Log(message); }

    public void Shutdown()
    {
        _logger.Log(new LogMessage(this, "Starting shutdown"));
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
