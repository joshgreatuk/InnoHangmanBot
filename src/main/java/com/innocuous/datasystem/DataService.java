package com.innocuous.datasystem;

import com.innocuous.datasystem.logging.LogMessage;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class is to be taken as a dependency in classes that need data
 * In their Initialization method they should register the database so DataService can load it
 */
public class DataService implements IDataService
{
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
}
