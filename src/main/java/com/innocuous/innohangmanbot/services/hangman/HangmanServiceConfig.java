package com.innocuous.innohangmanbot.services.hangman;

import com.innocuous.innoconfig.InnoConfigBase;

public class HangmanServiceConfig extends InnoConfigBase
{
    @Override
    public String GetConfigPath()
    {
        return "Data/HangmanServiceConfig";
    }

    public WordCategory[] categories = new WordCategory[] { new WordCategory("Category") };
}
