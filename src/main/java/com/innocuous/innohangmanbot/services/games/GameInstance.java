package com.innocuous.innohangmanbot.services.games;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.innocuous.innohangmanbot.services.hangman.HangmanInstance;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.function.Supplier;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({@JsonSubTypes.Type(HangmanInstance.class)})
public class GameInstance
{
    public String instanceID;

    public Long guildID;
    public Long channelID;
    public Long currentMessageID = 0L;

    @JsonIgnore
    public Message cachedMessage;

    @JsonIgnore
    public LocalDateTime lastInteracted;

    @JsonIgnore
    public Function<GameInstance, MessageCreateBuilder> messageSupplier;
    public String messageSupplierString;

    public GameInstance() { }
    public GameInstance(String instanceID, Long guildID, Long channelID, Function<GameInstance, MessageCreateBuilder> messageSupplier, String messageSupplierString)
    {
        this.instanceID = instanceID;
        this.guildID = guildID;
        this.channelID = channelID;
        this.messageSupplier = messageSupplier;
        this.messageSupplierString = messageSupplierString;
    }
}
