package com.innocuous.jdamodulesystem.data;

import com.innocuous.innoconfig.InnoConfigBase;

public class InteractionConfig extends InnoConfigBase
{
    @Override
    public String GetConfigPath() {
        return "Config/InteractionConfig.json";
    }

    public String commandPrefix = "!";
    public Boolean commandAutoRegister = true;
    public Boolean autoRegisterGlobally = false;
    public Long autoRegisterGuild = 0L;
}
