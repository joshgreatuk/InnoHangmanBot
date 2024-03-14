package com.innocuous.innohangmanbot.services.hangman;

import com.innocuous.innoconfig.InnoConfigBase;

public class GameInstanceServiceConfig extends InnoConfigBase
{
    @Override
    public String GetConfigPath() {
        return "Config/GameInstanceService.json";
    }

    public Boolean removeOldMessages;
}
