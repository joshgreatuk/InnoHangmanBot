package com.innocuous.innohangmanbot.services;

import com.innocuous.innologger.InnoLoggerService;
import com.innocuous.innologger.LogMessage;
import com.innocuous.innologger.LogSeverity;

import java.util.Optional;

public class InnoLoggerDSBridge
{
    private final InnoLoggerService _logger;

    public InnoLoggerDSBridge(InnoLoggerService logger)
    {
        _logger = logger;
        _logger.Log(new LogMessage(this, "Initialized"));
    }

    public void Log(com.innocuous.datasystem.logging.LogMessage message)
    {
        _logger.Log(new LogMessage(message.sender, message.message, ToInnoLoggerSeverity(message.severity), message.exception.isPresent() ? message.exception.get() : null), "DSBridge");
    }

    public com.innocuous.innologger.LogSeverity ToInnoLoggerSeverity(com.innocuous.datasystem.logging.LogSeverity severity)
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
