package com.innocuous.innohangmanbot.services.hangman;

import java.awt.*;

public enum HangmanStatus
{
    Running("Hangman", Color.gray),
    Won("You guessed it!", Color.GREEN),
    Lost("The word was ",Color.RED),
    Cancelled("Game cancelled", Color.BLUE);

    private final String title;
    private final Color colour;

    private HangmanStatus(String title, Color colour)
    {
        this.title = title;
        this.colour = colour;
    }

    public String getTitle()
    {
        return title;
    }
    public Color getColour()
    {
        return colour;
    }
}
