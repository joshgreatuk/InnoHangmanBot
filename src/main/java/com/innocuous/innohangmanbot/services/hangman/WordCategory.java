package com.innocuous.innohangmanbot.services.hangman;

public class WordCategory
{
    public String name;
    public String[] wordList = new String[0];

    public WordCategory(String name)
    {
        this.name = name;
    }
}
