package com.innocuous.innohangmanbot.services;

import com.innocuous.dependencyinjection.servicedata.IInitializable;
import com.innocuous.innologger.ILogger;
import com.innocuous.innologger.InnoLoggerService;
import com.innocuous.innologger.LogMessage;
import com.innocuous.innologger.LogSeverity;

public class ErrorThrower extends InnoService implements IInitializable
{
    public ErrorThrower(ILogger logger)
    {
        super(logger);
    }

    @Override
    public void Initialize()
    {
        _logger.Log(new LogMessage(this, "Throwing Error", LogSeverity.Error, new Exception("Forced Exception")));
        _logger.Log(new LogMessage(this, "Throwing Critical", LogSeverity.Critical, new Exception("Forced Exception")));
    }
}
