package com.innocuous.innologger;

import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

// This is the default fallback logger, for when a service isn't given an ILogger instance
public class ConsoleLogger implements ILogger
{
    public ConsoleLogger()
    {
        messageTimeFormatter = DateTimeFormatter.ISO_TIME;
    }

    private final DateTimeFormatter messageTimeFormatter;
    private final String colourReset = "\u001B[0m";

    @Override
    public void Log(@NotNull LogMessage message)
    {
        System.out.print(ParseLogMessage(message, true));
    }

    public String ParseLogMessage(LogMessage message, Boolean useColours)
    {
        String formattedTime = messageTimeFormatter.format(LocalTime.now());
        String colourString = message.severity.getColour();
        String logMessage = (useColours ? colourString : "")
                + formattedTime + " " + getClass().getSimpleName() + (message.bridge.isPresent() ? "(" + message.bridge.get() + ")" : "")
                + " [" + message.sender + "]"
                + "[" + message.severity.name() + "] "
                + message.message
                + (message.exception.isPresent() ? (" : " + message.exception.get()) : "")
                + (useColours ? colourReset : "") + "\n";

        if (message.exception.isEmpty()) return logMessage;

        StackTraceElement[] stackTrace = message.exception.get().getStackTrace();
        for (StackTraceElement element : stackTrace)
        {
            logMessage += (useColours ? colourString : "") + "\t" + element.toString() + (useColours ? colourReset : "") + "\n";
        }

        return logMessage;
    }
}
