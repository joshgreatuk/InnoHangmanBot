package com.innocuous.innohangmanbot.services.hangman;

import com.innocuous.innohangmanbot.services.InnoService;
import com.innocuous.innologger.ILogger;

public class HangmanService extends InnoService
{
    public HangmanService(ILogger logger, HangmanServiceConfig config)
    {
        super(logger);
        _config = config;
    }

    private final HangmanServiceConfig _config;

    public String GenerateHangmanGameID(Long guildID, Long channelID)
    {
        return "Hangman." + guildID + "." + channelID;
    }
}
