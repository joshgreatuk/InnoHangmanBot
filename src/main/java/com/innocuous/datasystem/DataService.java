package com.innocuous.datasystem;

import com.innocuous.datasystem.logging.LogMessage;
import com.innocuous.datasystem.logging.LogSeverity;
import com.innocuous.dependencyinjection.IServiceProvider;
import com.innocuous.dependencyinjection.servicedata.IInitializable;
import com.innocuous.dependencyinjection.servicedata.IStoppable;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class is to be taken as a dependency in classes that need data
 * In their Initialization method they should register the database so DataService can load it
 */
public class DataService implements IDataService
{
    private IDataProvider dataProvider;

    public DataService()
    {
        _logConsumer = Optional.empty();
    }
    public DataService(Consumer<LogMessage> logConsumer)
    {
        _logConsumer = Optional.of(logConsumer);
        Log(new LogMessage(this, "Log consumer configured"));
    }

    private final Optional<Consumer<LogMessage>> _logConsumer;

    public void Log(LogMessage message)
    {
        if (_logConsumer == null || _logConsumer.isEmpty()) return;
        _logConsumer.get().accept(message);
    }

    @Override
    public void RegisterDatabase(Database<?, ?> database)
    {

    }

    @Override
    public void RegisterDataProvider(IDataProvider provider)
    {
        if (dataProvider != null)
        {
            Log(new LogMessage(this,
                    "Tried to register two DataProviders, using '" + provider.getClass().getName() + "' ",
                    LogSeverity.Warning));

            if (IServiceProvider.HasInterface(dataProvider.getClass(), IStoppable.class))
            {
                IStoppable stoppable = (IStoppable)dataProvider;
                stoppable.Shutdown();
            }
        }

        //Provider should be initialized during services startup
        dataProvider = provider;
    }
}
