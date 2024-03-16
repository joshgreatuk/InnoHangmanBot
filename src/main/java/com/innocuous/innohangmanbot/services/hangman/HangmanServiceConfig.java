package com.innocuous.innohangmanbot.services.hangman;

import com.innocuous.innoconfig.InnoConfigBase;

import java.util.ArrayList;
import java.util.List;

public class HangmanServiceConfig extends InnoConfigBase
{
    @Override
    public String GetConfigPath()
    {
        return "Data/HangmanServiceConfig.json";
    }

    public String[] hangmanStages = new String[11];
    public WordCategory[] categories = new WordCategory[]
            {
                    new WordCategory("All Categories"),
                    new WordCategory("Random Category")
            };
}
