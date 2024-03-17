package com.innocuous.innohangmanbot.services.hangman;

import com.innocuous.innohangmanbot.services.games.GameInstance;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.ArrayList;
import java.util.function.Function;

public class HangmanInstance extends GameInstance
{
    public HangmanInstance() { }
    public HangmanInstance(String instanceID, Long guildID, Long channelID, Long superChannelID, Function<GameInstance, MessageCreateBuilder> messageSupplier, String messageSupplierString)
    {
        super(instanceID, guildID, channelID, superChannelID, messageSupplier, messageSupplierString);

        this.category = category;
        this.word = word;
    }

    public String category;
    public String word;
    public HangmanStatus status = HangmanStatus.Running;

    public Integer guessesRemaining = 11;

    public ArrayList<Character> rightChars = new ArrayList<>();
    public ArrayList<Character> wrongChars = new ArrayList<>();
}
