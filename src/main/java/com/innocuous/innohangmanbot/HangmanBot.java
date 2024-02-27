package com.innocuous.innohangmanbot;

import com.innocuous.dependencyinjection.*;
import com.innocuous.innohangmanbot.services.InnoService;
import com.innocuous.innologger.*;

import java.util.Optional;

public class HangmanBot
{
    public static void main(String[] args)
    {
        Optional<HangmanBot> botInstance = null;
        try
        {
            botInstance = Optional.of(new HangmanBot());
        }
        catch (Exception ex)
        {
            if (botInstance.isPresent())
            {
                botInstance.get().Shutdown();
            }
        }
    }

    private final IServiceProvider _services;
    private final InnoLoggerService _logger;

    public HangmanBot()
    {
        _services = BuildServiceCollection();
        _logger = _services.GetService(InnoLoggerService.class);
    }

    public IServiceProvider BuildServiceCollection()
    {
        return new ServiceCollection()
            .Build();
    }

    public void Log(LogMessage message)
    {  }

    public void Shutdown()
    {
        //TO-DO: Shutdown major services, close streams, etc
    }
}
