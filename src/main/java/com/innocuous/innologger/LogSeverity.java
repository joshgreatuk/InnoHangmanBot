package com.innocuous.innologger;

import java.awt.Color;
import java.util.Optional;

public enum LogSeverity
{
    Critical(0, Optional.of(Color.orange)),
    Error(0, Optional.of(Color.red)),
    Info(0, Optional.empty()),
    Warning(1, Optional.of(Color.yellow)),
    Debug(2, Optional.of(Color.lightGray)),
    Verbose(3, Optional.of(Color.gray));


    private final int value;
    private final Color colour;
    private LogSeverity(int value, Optional<Color> colour)
    {
        this.value = value;
        this.colour = colour.isPresent() ? colour.get() : Color.white;
    }

    public int getValue()
    {
        return value;
    }

    public Color getColour()
    {
        return colour;
    }
}
