package com.innocuous.innohangmanbot.services;

import com.innocuous.dependencyinjection.IServiceProvider;
import com.innocuous.dependencyinjection.servicedata.IInitializable;
import com.innocuous.dependencyinjection.servicedata.IStoppable;
import com.innocuous.innologger.InnoLoggerConfig;
import com.innocuous.innologger.InnoLoggerService;
import com.innocuous.innologger.LogMessage;
import com.innocuous.innologger.LogSeverity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;

public class InitializationService extends ListenerAdapter
{
    private final IServiceProvider _services;
    private final InnoLoggerService _logger;

    public InitializationService(IServiceProvider services, InnoLoggerService logger)
    {
        _services = services;
        _logger = logger;
    }

    @Override
    public void onReady(ReadyEvent event)
    {
        for (Object service : _services.GetActiveServices())
        {
            if (!service.getClass().isAssignableFrom(IInitializable.class)) continue;
            IInitializable initService = (IInitializable)service;

            _logger.Log(new LogMessage(initService, "Initializing service", LogSeverity.Debug));

            try
            {
                initService.Initialize();
            }
            catch (Exception ex)
            {
                _logger.Log(new LogMessage(initService, "Caught an exception during Initialization", LogSeverity.Error, ex));
            }
        }
    }
}
