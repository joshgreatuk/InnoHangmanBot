package com.innocuous.innologger;

import java.util.Optional;

public class LogMessage
{
    public String sender;
    public String message;
    public LogSeverity severity;
    public Optional<Throwable> exception;
    public Optional<String> bridge = Optional.empty();

    public LogMessage(Object sender, String message)
    { this(sender.getClass().getName(), message, LogSeverity.Info); }
    public LogMessage(String sender, String message)
    { this(sender, message, LogSeverity.Info); }

    public LogMessage(Object sender, String message, LogSeverity severity)
    { this(sender.getClass().getName(), message, severity, null); }
    public LogMessage(String sender, String message, LogSeverity severity)
    { this(sender, message, severity, null); }

    public LogMessage(Object sender, String message, LogSeverity severity, Throwable exception)
    { this(sender.getClass().getName(), message, severity, exception); }
    public LogMessage(String sender, String message, LogSeverity severity, Throwable exception)
    {
        this.sender = sender;
        this.message = message;
        this.severity = severity;
        this.exception = exception == null ? Optional.empty() : Optional.of(exception);
    }
}
