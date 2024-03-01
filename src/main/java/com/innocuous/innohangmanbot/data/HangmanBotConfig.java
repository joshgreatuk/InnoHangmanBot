package com.innocuous.innohangmanbot.data;

import com.innocuous.innoconfig.InnoConfigBase;

public class HangmanBotConfig extends InnoConfigBase
{
    public Boolean debugMode = true;

    public String debugBotToken = "";
    public String releaseBotToken = "";

    @Override
    public String GetConfigPath() { return "Config/HangmanBotConfig.json"; }
}
