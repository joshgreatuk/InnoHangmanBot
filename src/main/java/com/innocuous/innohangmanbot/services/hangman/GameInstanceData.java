package com.innocuous.innohangmanbot.services.hangman;

import com.innocuous.innoconfig.InnoConfigBase;

import java.util.Hashtable;

public class GameInstanceData extends InnoConfigBase
{
    @Override
    public String GetConfigPath() {
        return "Data/GameInstances.json";
    }

    public Hashtable<String, GameInstance> instances = new Hashtable<>();
}
