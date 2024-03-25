package com.innocuous.innohangmanbot.services;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import com.innocuous.innologger.ILogger;
import com.innocuous.innologger.LogMessage;
import com.innocuous.innologger.LogSeverity;
import ch.qos.logback.classic.Level;

public class SLF4JBridgeAppender extends AppenderBase<ILoggingEvent>
{
    public static ILogger logger;

    @Override
    protected void append(ILoggingEvent o)
    {
        if (logger == null) return;
        IThrowableProxy throwProxy = o.getThrowableProxy();
        Throwable thrower = throwProxy != null ? ((ThrowableProxy)throwProxy).getThrowable() : null;
        LogMessage message = new LogMessage(o.getLoggerName(), o.getMessage(), GetLevelSeverity(o.getLevel()), thrower);
        logger.Log(message);
    }

    private LogSeverity GetLevelSeverity(Level level)
    {
        return switch (level.levelStr)
        {
            case "ERROR" -> LogSeverity.Error;
            case "WARN" -> LogSeverity.Warning;
            case "INFO" -> LogSeverity.Info;
            case "DEBUG" -> LogSeverity.Debug;
            case "TRACE" -> LogSeverity.Verbose;
            default -> LogSeverity.Critical;
        };
    }
}
