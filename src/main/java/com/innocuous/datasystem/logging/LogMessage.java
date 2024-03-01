package com.innocuous.datasystem.logging;

import java.util.Optional;

public class LogMessage
{
    public Object sender;
    public String message;
    public LogSeverity severity;
    public Optional<Exception> exception = Optional.empty();

    public LogMessage(Object sender, String message)
    {
        this(sender, message, LogSeverity.Info);
    }
    public LogMessage(Object sender, String message, LogSeverity severity)
    {
        this.sender = sender;
        this.message = message;
        this.severity = severity;
    }
    public LogMessage(Object sender, String message, LogSeverity severity, Exception exception)
    {
        this(sender, message, severity);
        this.exception = Optional.of(exception);
    }
}
