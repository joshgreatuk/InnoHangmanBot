package com.innocuous.innologger;

import org.jetbrains.annotations.NotNull;

public interface ILogger
{
    public void Log(@NotNull LogMessage message);
}
