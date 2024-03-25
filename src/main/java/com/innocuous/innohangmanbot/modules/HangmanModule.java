package com.innocuous.innohangmanbot.modules;

import com.innocuous.innohangmanbot.services.hangman.HangmanService;
import com.innocuous.innohangmanbot.services.hangman.HangmanStatus;
import com.innocuous.jdamodulesystem.JDAModuleBase;
import com.innocuous.jdamodulesystem.annotations.Description;
import com.innocuous.jdamodulesystem.annotations.SlashCommand;
import com.innocuous.jdamodulesystem.annotations.components.ButtonComponent;
import com.innocuous.jdamodulesystem.annotations.components.StringSelectComponent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HangmanModule extends JDAModuleBase
{
    protected final HangmanService _hangmanService;

    public HangmanModule(HangmanService hangmanService)
    {
        _hangmanService = hangmanService;
    }

    //Slash Commands
    @SlashCommand(name = "create-game", description = "Create a hangman game in a new thread")
    public void SetupGameCommand()
    {
        if (commandInteraction.isFromGuild() && !PermissionUtil.checkPermission(
                commandInteraction.getGuildChannel().getPermissionContainer(),
                commandInteraction.getGuild().getMember(commandInteraction.getJDA().getSelfUser()),
                Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_SEND_IN_THREADS, Permission.CREATE_PUBLIC_THREADS))
        {
            FailGuess("Bot needs VIEW_CHANNEL, MESSAGE_SEND, MESSAGE_SEND_IN_THREADS and CREATE_PUBLIC_THREADS", true);
        }

        if (commandInteraction.getChannelType().isThread())
        {
            FailGuess("You can't create a new game in a game thread!", true);
            return;
        }

        if (commandInteraction.isFromGuild() && _hangmanService.InstancesWithGuild(commandInteraction.getGuild().getIdLong()) >= 15)
        {
            FailGuess("Sorry, you can only have 15 active hangman instances in 1 guild at a time", true);
            return;
        }
        else if (_hangmanService.InstancesWithChannel(commandInteraction.getChannelIdLong()) >= 3)
        {
            FailGuess("Sorry, you can only have 3 active hangman instances per channel at a time)", true);
            return;
        }

        InteractionHook hook = commandInteraction.reply("Creating game!").complete();
        Message message = hook.retrieveOriginal().completeAfter(100L, TimeUnit.MILLISECONDS);
        long channelID = commandInteraction.getChannelIdLong();
        long guildID = 0;

        if (commandInteraction.isFromGuild())
        {
            ThreadChannel channel = message.createThreadChannel("Hangman!").complete();
            channelID = channel.getIdLong();
            channel.join().queue();
            channel.addThreadMember(commandInteraction.getUser()).queue();
            guildID = commandInteraction.getGuild().getIdLong();
        }

        String newGameID = _hangmanService.GenerateHangmanGameID(guildID, channelID);
        _hangmanService.StartGameSetup(newGameID, guildID, channelID, commandInteraction.getChannelIdLong());
    }

    @SlashCommand(name = "guess", description = "Make a guess")
    public void GuessCommand(@Description(description = "Guess a letter or a word") String guess)
    {
        if (!_hangmanService.GameExists(GetGameID()))
        {
            FailGuess("Please create a game with /create-game first", true);
            return;
        }

        if (guess.length() < 2 && !Character.isLetter(guess.charAt(0)) )
        {
            FailGuess("Guess must be a letter or a word", true);
            return;
        }

        //Check guess length, if char been used, etc
        if (guess.length() < 2 && _hangmanService.IsCharUsed(GetGameID(), guess.charAt(0)))
        {
            FailGuess(guess + " has already been guessed!", true);
            return;
        }

        //Reply right or wrong
        boolean guessCorrect = _hangmanService.MakeGuess(GetGameID(), guess.toLowerCase());
        if (!guessCorrect)
        {
            if (guess.length() > 1)
            {
                FailGuess(guess + " is incorrect!",false);
                return;
            }
            FailGuess(guess + " isn't in the word!", false);
            return;
        }

        if (guess.length() > 1)
        {
            commandInteraction.reply(new MessageCreateBuilder()
                    .setEmbeds(new EmbedBuilder()
                            .setTitle(guess + " is correct! Well done!")
                            .setColor(Color.GREEN).build())
                    .build()).complete();
            return;
        }

        commandInteraction.reply(new MessageCreateBuilder()
                .setEmbeds(new EmbedBuilder()
                        .setTitle(guess + " is in the word!")
                        .setColor(Color.GREEN).build())
                .build()).complete();
    }

    private void FailGuess(String message, Boolean ephemeral)
    {
        commandInteraction.reply(new MessageCreateBuilder()
                .setEmbeds(new EmbedBuilder()
                        .setTitle(message)
                        .setColor(Color.RED).build())
                .build()).setEphemeral(ephemeral).queue();
        _hangmanService.UpdatePage(GetGameID());
    }

    @SlashCommand(name = "end-game", description = "End the current game")
    public void EndGameCommand()
    {
        if (!_hangmanService.GameExists(GetGameID()))
        {
            FailGuess("Please create a game with /create-game first", true);
            return;
        }

        //End the game
        _hangmanService.EndGame(GetGameID(), HangmanStatus.Cancelled);
        commandInteraction.reply("Ending game!").complete().deleteOriginal().queue();
    }

    //Game Setup Page
    @StringSelectComponent(customID = "hangman.select-category")
    public void SelectWordCategory(List<String> selectedCategory)
    {
        componentInteraction.reply("Category Selected!").complete().deleteOriginal().queue();
        String selected = selectedCategory.get(0);
        _hangmanService.UpdateGameCategory(GetGameID(), selected);
        if (componentInteraction.isFromGuild())
        {
            ThreadChannel channel = (ThreadChannel) componentInteraction.getMessageChannel();
            channel.getManager().setName("Hangman! Category: " + selected).queue();
        }
    }

    @ButtonComponent(customID = "hangman.start-game")
    public void StartGameButton()
    {
        componentInteraction.reply("Starting game!").complete().deleteOriginal().queue();
        _hangmanService.StartGame(GetGameID());
    }

    @ButtonComponent(customID = "hangman.cancel-game")
    public void CancelGameButton()
    {
        componentInteraction.reply("Ending game!").complete().deleteOriginal().queue();
        _hangmanService.EndGame(GetGameID(), HangmanStatus.Cancelled);

    }

    //Game End Page
    @ButtonComponent(customID = "hangman.playagain:yes")
    public void PlayAgainButton()
    {
        _hangmanService.PlayAgain(GetGameID());
        componentInteraction.reply("Playing again!").complete().deleteOriginal().queue();
    }

    @ButtonComponent(customID = "hangman.playagain:no")
    public void DontPlayAgainButton()
    {
        componentInteraction.reply("Ending instance!").complete().deleteOriginal().queue();
        _hangmanService.DontPlayAgain(GetGameID());

        if (componentInteraction.isFromGuild())
        {
            ((ThreadChannel) componentInteraction.getChannel()).getManager().setLocked(true).setArchived(true).queue();
        }

    }

    private String GetGameID()
    {
        return commandInteraction != null ? _hangmanService.GenerateHangmanGameID(
                (commandInteraction.isFromGuild() ? commandInteraction.getGuild().getIdLong() : 0),
                commandInteraction.getChannelIdLong())
                : _hangmanService.GenerateHangmanGameID(
                (componentInteraction.isFromGuild() ? componentInteraction.getGuild().getIdLong() : 0),
                componentInteraction.getChannelIdLong());
    }
}
