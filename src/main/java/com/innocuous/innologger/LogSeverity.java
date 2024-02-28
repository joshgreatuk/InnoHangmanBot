package com.innocuous.innologger;

import java.awt.Color;
import java.util.Optional;

public enum LogSeverity
{
    Critical(0, Optional.of("\u001B[38;5;166m")),
    Error(0, Optional.of("\u001B[1m")),
    Info(0, Optional.empty()),
    Warning(1, Optional.of("\u001B[3m")),
    Debug(2, Optional.of("\u001B[38;5;250m")),
    Verbose(3, Optional.of("\u001B[38;5;244m"));


    private final int value;
    private final String ansiColour;
    private LogSeverity(int value, Optional<String> colour)
    {
        this.value = value;
        this.ansiColour = colour.isPresent() ? colour.get() : "\u001B[37m";
    }

    public int getValue()
    {
        return value;
    }

    public String getColour()
    {
        return ansiColour;
    }
}
