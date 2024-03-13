package com.innocuous.innohangmanbot.services;

import com.innocuous.dependencyinjection.IServiceProvider;
import com.innocuous.dependencyinjection.servicedata.IInitializable;
import com.innocuous.dependencyinjection.servicedata.IStoppable;
import com.innocuous.innologger.*;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.Arrays;

public class InitializationService extends InnoService implements IJDAEventListener
{
    private final IServiceProvider _services;

    public InitializationService(IServiceProvider services, ILogger logger)
    {
        super(logger);
        _services = services;
    }

    @SubscribeEvent
    public void onReady(ReadyEvent event)
    {
        _logger.Log(new LogMessage(this, "Service Initialization started"));
        for (IInitializable initService : _services.<IInitializable>GetServicesWithInterface(IInitializable.class))
        {
            _logger.Log(new LogMessage(initService, "Initializing service", LogSeverity.Debug));

            try
            {
                initService.Initialize();
                initService.Initialize(event);
            }
            catch (Exception ex)
            {
                _logger.Log(new LogMessage(initService, "Caught an exception during initialization", LogSeverity.Error, ex));
            }
        }
        _logger.Log(new LogMessage(this, "Service initialization finished"));
    }
}
