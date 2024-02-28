package com.innocuous.innologger;

import okio.Path;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.server.ExportException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class InnoLoggerService
{
    private final String colourReset = "\u001B[0m";
    private final InnoLoggerConfig _config;
    private final DateTimeFormatter messageTimeFormatter;
    private final DateTimeFormatter logFileTimeFormatter;
    private FileWriter writer;

    public InnoLoggerService(InnoLoggerConfig config)
    {
        _config = config;
        messageTimeFormatter = DateTimeFormatter.ofPattern(_config.timeFormat);
        logFileTimeFormatter = DateTimeFormatter.ofPattern(_config.fileTimeFormat);

        SwitchLogFile();
    }

    public void Log(LogMessage message)
    {
        String formattedTime = messageTimeFormatter.format(LocalTime.now());
        String parsedMessage = message.severity.getColour()
                + "[" + message.sender + "]["
                + formattedTime + "] "
                + message.message
                + (message.exception.isPresent() ? (" : " + message.exception) : "")
                + colourReset + "\n";

        System.out.print(parsedMessage);
        try
        {
            writer.write(parsedMessage);
            writer.flush();
        }
        catch (Exception ex) { throw new RuntimeException(ex); }
    }

    public void Shutdown()
    {
        try
        {
            writer.write("Closing log, goodnight");
            writer.close();
        }
        catch (Exception ex) { throw new RuntimeException(ex); }
    }

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
            String rawPath = Paths.get("").toAbsolutePath().toString();
            String path = "";

            Integer i = 0;
            while (!path.isEmpty() && !Files.exists(Paths.get(path)))
            {
                i++;
                path = rawPath + fileName + "-" + i + ".log";
            }

            //TO-DO: Check paths exist and create directories that dont recursively

            writer = new FileWriter(path);
            Log(new LogMessage(this, "Log opened, hi! :D"));
        }
        catch (Exception ex) { throw new RuntimeException(ex); };
    }
}
