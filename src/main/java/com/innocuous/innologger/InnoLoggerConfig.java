package com.innocuous.innologger;

import com.innocuous.innoconfig.InnoConfigBase;

public class InnoLoggerConfig extends InnoConfigBase
{
    public String appName = "InnoApplication";
    public String logPath = "Logs/";

    public String fileTimeFormat = "dd-MM-yyyy";
    public String timeFormat = "HH:mm:ss";

    public LogSeverity logLevel = LogSeverity.Info;

    public Boolean logFilePadding = false;

    public Integer severityPadding = 8;
    public Integer classPadding = 50;

    @Override
    public String GetConfigPath(){
        return "Config/InnoLogger.json";
    }
}
