package com.innocuous.innohangmanbot.services;

import com.innocuous.innologger.InnoLoggerService;
import com.innocuous.innologger.LogMessage;
import com.innocuous.innologger.LogSeverity;

import java.util.Optional;

public class InnoLoggerDIBridge
{
    private final InnoLoggerService _logger;

    public InnoLoggerDIBridge(InnoLoggerService logger)
    {
        _logger = logger;
        _logger.Log(new LogMessage(this, "Initialized"));
    }

    public void Log(com.innocuous.dependencyinjection.logging.LogMessage message)
    {
        _logger.Log(new LogMessage("(InnoLoggerDIBridge)"+message.sender, message.message, ToInnoLoggerSeverity(message.severity), message.exception.isPresent() ? message.exception.get() : null));
    }

    public com.innocuous.innologger.LogSeverity ToInnoLoggerSeverity(com.innocuous.dependencyinjection.logging.LogSeverity severity)
    {
        return switch(severity)
        {
            case Critical -> LogSeverity.Critical;
            case Error -> LogSeverity.Error;
            case Info -> LogSeverity.Info;
            case Warning -> LogSeverity.Warning;
            case Debug -> LogSeverity.Debug;
            case Verbose -> LogSeverity.Verbose;
        };
    }
}
