package com.innocuous.innohangmanbot.services.hangman;

import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.util.function.Supplier;

public class GameInstance
{
    public String instanceID;

    public Long guildID;
    public Long channelID;
    public Long currentMessageID;

    public Supplier<MessageCreateBuilder> messageSupplier;

    public GameInstance(String instanceID, Long guildID, Long channelID, Supplier<MessageCreateBuilder> messageSupplier)
    {
        this.instanceID = instanceID;
        this.guildID = guildID;
        this.channelID = channelID;
        this.messageSupplier = messageSupplier;
    }
}
