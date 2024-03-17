package com.innocuous.innohangmanbot.services.games;

import com.innocuous.innoconfig.InnoConfigBase;
import com.innocuous.jdamodulesystem.InteractionService;

public class GameInstanceServiceConfig extends InnoConfigBase
{
    @Override
    public String GetConfigPath() {
        return "Config/GameInstanceService.json";
    }

    public Boolean removeOldMessages = true;
    public Integer timeoutMinutes = 60;
}
