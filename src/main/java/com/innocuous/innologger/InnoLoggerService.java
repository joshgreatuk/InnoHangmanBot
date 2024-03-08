package com.innocuous.innologger;

import net.dv8tion.jda.api.entities.Icon;
import okio.Path;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class InnoLoggerService implements ILogger
{
    private final String colourReset = "\u001B[0m";
    private final InnoLoggerConfig _config;
    private final DateTimeFormatter messageTimeFormatter;
    private final DateTimeFormatter logFileTimeFormatter;
    private FileWriter writer;

    public InnoLoggerService(InnoLoggerConfig config)
    {
        _config = config;
        _config.Init();
        messageTimeFormatter = DateTimeFormatter.ofPattern(_config.timeFormat);
        logFileTimeFormatter = DateTimeFormatter.ofPattern(_config.fileTimeFormat);

        SwitchLogFile();
    }

    public void Log(@NotNull LogMessage message, @NotNull String bridge)
    {
        message.bridge = Optional.of(bridge);
        Log(message);
    }
    public void Log(@NotNull LogMessage message)
    {
        if (message.severity.getValue() > _config.logLevel.getValue()) return;

        System.out.print(ParseLogMessage(message, true, true));
        try
        {
            writer.write(ParseLogMessage(message, false, _config.logFilePadding));

            if (message.exception.isPresent())
            {
               //throw new RuntimeException(message.exception.get());
            }

            writer.flush();
        }
        catch (Exception ex) { throw new RuntimeException(ex); }
    }

    public String ParseLogMessage(LogMessage message, Boolean useColours, Boolean usePadding)
    {
        String formattedTime = messageTimeFormatter.format(LocalTime.now());
        String colourString = message.severity.getColour();
        String logMessage = (useColours ? colourString : "")
                + formattedTime + " " + getClass().getSimpleName() + (message.bridge.isPresent() ? "(" + message.bridge.get() + ")" : "")
                + " [" + (usePadding ? ApplyPadding(message.sender, _config.classPadding) : message.sender) + "]"
                + "[" + (usePadding ? ApplyPadding(message.severity.name(), _config.severityPadding)  : message.severity.name()) + "] "
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

    public void Shutdown()
    {
        try
        {
            Log(new LogMessage(this, "Log closed, goodnight"));
            writer.close();
        }
        catch (Exception ex) { throw new RuntimeException(ex); }
    }

    public String ApplyPadding(String message, Integer padding)
    {
        if (message.length() == padding) return message;
        else if (message.length() < padding)
        {
            //If not enough characters, pad end with blankspace
            Integer messageLength = message.length();
            for (int i=0; i < padding - messageLength; i++)
            {
                message += " ";
            }
        }
        else if (message.length() > padding)
        {
            //If too many characters, cut start out by difference plus 2 or 3 and add ... at the start
            message = message.subSequence(message.length()-padding+3, message.length()).toString();
            message = "..." + message;
        }
        return message;
    }

    @NotNull
    private String GetLogFileName()
    {
        String date = logFileTimeFormatter.format(LocalDate.now());
        return _config.appName + "-" + date;
    }

    private void SwitchLogFile()
    {
        try
        {
            if (writer != null)
            {
                writer.write("Closing log, goodnight");
                writer.close();
            }

            String fileName = GetLogFileName();
            String rawPath = Paths.get("").toAbsolutePath().toString() + "/" +_config.logPath;
            if (!Files.exists(Paths.get(rawPath))) Files.createDirectories(Paths.get(rawPath));

            String path = "";

            Integer i = 0;
            while (path.isEmpty() || Files.exists(Paths.get(path)))
            {
                i++;
                path = rawPath + fileName + "-" + i + ".log";
            }

            Files.createFile(Paths.get(path));
            writer = new FileWriter(path);
            Log(new LogMessage(this, "Log opened, hi! :D"));
        }
        catch (Exception ex) { throw new RuntimeException(ex); };
    }
}
