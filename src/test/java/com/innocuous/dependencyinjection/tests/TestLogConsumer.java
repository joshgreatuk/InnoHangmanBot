package com.innocuous.dependencyinjection.tests;

import com.innocuous.dependencyinjection.logging.LogMessage;
import com.innocuous.dependencyinjection.logging.LogSeverity;

import java.util.Hashtable;

public class TestLogConsumer
{
    public Hashtable<LogSeverity, Integer> logCount = new Hashtable<LogSeverity, Integer>();

    public void Log(LogMessage message)
    {
        if (!logCount.containsKey(message.severity)) logCount.put(message.severity, 0);
        logCount.put(message.severity, logCount.get(message.severity) + 1);
    }
}
