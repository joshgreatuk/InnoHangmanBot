package com.innocuous.innohangmanbot.services.hangman;

import com.innocuous.dependencyinjection.servicedata.IInitializable;
import com.innocuous.innohangmanbot.services.InnoService;
import com.innocuous.innohangmanbot.services.games.GameInstance;
import com.innocuous.innohangmanbot.services.games.GameInstanceService;
import com.innocuous.innologger.ILogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.awt.*;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/*
    Game flow:
    - Create thread, reply with "Game created!", create instance with thread channel id
    - Setup screen (select category, play button, cancel button)
    - Game screen (/guess, /endgame)
    - If player ends game or runs out of guesses end the game, ask if play again
    - If play again, go back to setup screen
    - If not, lock thread and remove instance
    - If player guesses correctly tell them they win and ask if play again
 */
public class HangmanService extends InnoService
{
    public HangmanService(ILogger logger, HangmanServiceConfig config, GameInstanceService instanceService)
    {
        super(logger);
        _config = config;
        _config.Init();
        _instanceService = instanceService;
        _random = new Random(LocalTime.now().getNano());

        //Iterate through HangmanInstances and translate suppliers
        for (HangmanInstance instance : _instanceService.GetInstances().stream()
                .filter(x -> x.getClass().equals(HangmanInstance.class)).map(x -> (HangmanInstance)x).toList())
        {
            instance.messageSupplier = switch (instance.messageSupplierString)
            {
                case "SupplySetupScreen" -> this::SupplySetupScreen;
                case "SupplyGame" -> this::SupplyGame;
                case "SupplyEndScreen" -> this::SupplyEndScreen;
                case "SupplyThankYouScreen" -> this::SupplyThankYouScreen;
                default -> null;
            };
        }
    }

    private final HangmanServiceConfig _config;
    private final GameInstanceService _instanceService;
    private final Random _random;

    public void StartGameSetup(String gameID, Long guildID, Long channelID, Long superChannelID)
    {
        //Create instance, refresh message. Thread created by Module
        _instanceService.RegisterInstance(new HangmanInstance(gameID, guildID, channelID, superChannelID, this::SupplySetupScreen, "SupplySetupScreen"));
    }

    public void StartGame(String gameID)
    {
        HangmanInstance instance = (HangmanInstance)_instanceService.GetInstance(gameID);
        String category = instance.category;

        //Remember to process All and Random separately
        if (category.equals("Random Category"))
        {
            category = _config.categories[_random.nextInt(2, _config.categories.length)].name;
        }

        String finalCategory = category;
        WordCategory wordCategory = Arrays.stream(_config.categories).filter(x -> x.name.equals(finalCategory)).toList().get(0);
        List<String> wordList = Arrays.stream(wordCategory.wordList).toList();

        if (category.equals("All Categories"))
        {
            wordList = Arrays.stream(_config.categories).flatMap(x -> Arrays.stream(x.wordList)).toList();
        }

        instance.category = category;
        instance.word = GetRandomWord(wordList);

        _instanceService.SetInstanceSupplier(gameID, this::SupplyGame, "SupplyGame");
        _instanceService.RefreshInstance(gameID);
    }

    public Boolean MakeGuess(String gameID, String guess) //Assumed guess is already preconditioned
    {
        //Return if guess is correct
        HangmanInstance instance = (HangmanInstance)_instanceService.GetInstance(gameID);
        if (guess.length() < 2)
        {
            //Guess letter
            if (instance.word.chars().anyMatch(x -> x == guess.charAt(0)))
            {
                instance.rightChars.add(guess.charAt(0));
                _instanceService.RefreshInstance(gameID);

                if (instance.word.chars().allMatch(x -> instance.rightChars.contains((char)x)))
                {
                    EndGame(gameID, HangmanStatus.Won);
                }

                return true;
            }

            //Wrong letter
            instance.wrongChars.add(guess.charAt(0));
            _instanceService.RefreshInstance(gameID);
            instance.guessesRemaining--;
            if (instance.guessesRemaining <= 0)
            {
                EndGame(gameID, HangmanStatus.Lost);
            }
            return false;
        }
        else
        {
            //Guess word
            if (guess.equals(instance.word))
            {
                //You win!
                EndGame(gameID, HangmanStatus.Won);
                return true;
            }

            //You guess wrong
            instance.guessesRemaining--;
            if (instance.guessesRemaining < 1)
            {
                EndGame(gameID, HangmanStatus.Lost);
            }
            return false;
        }
    }

    public Boolean IsCharUsed(String gameID, char character)
    {
        HangmanInstance instance = (HangmanInstance)_instanceService.GetInstance(gameID);
        return instance.wrongChars.contains(character) || instance.rightChars.contains(character);
    }

    public int InstancesWithGuild(Long guildID)
    {
        return _instanceService.InstancesWithGuild(guildID);
    }

    public int InstancesWithChannel(Long channelID)
    {
        return _instanceService.InstancesWithChannel(channelID);
    }

    public Boolean GameExists(String gameID)
    {
        return _instanceService.InstanceExists(gameID);
    }

    public void UpdateGameCategory(String gameID, String newCategory)
    {
        HangmanInstance instance = (HangmanInstance)_instanceService.GetInstance(gameID);
        instance.category = newCategory;
        _instanceService.RefreshInstance(gameID);
    }

    public void EndGame(String gameID, HangmanStatus status)
    {
        //As a result of the game ending or being cancelled
        HangmanInstance instance = (HangmanInstance)_instanceService.GetInstance(gameID);
        instance.status = status;
        _instanceService.SetInstanceSupplier(gameID, this::SupplyEndScreen, "SupplyEndScreen");
        _instanceService.RefreshInstance(gameID);
    }

    public void PlayAgain(String gameID)
    {
        HangmanInstance instance = (HangmanInstance)_instanceService.GetInstance(gameID);
        instance.guessesRemaining = 10;
        instance.wrongChars.clear();
        instance.rightChars.clear();

        _instanceService.SetInstanceSupplier(gameID, this::SupplySetupScreen, "SupplySetupScreen");
        _instanceService.RefreshInstance(gameID);
    }

    public void DontPlayAgain(String gameID)
    {
        _instanceService.SetInstanceSupplier(gameID, this::SupplyThankYouScreen, "SupplyThankYouScreen");
        _instanceService.RefreshInstance(gameID);

        _instanceService.CloseInstance(gameID);
    }

    public void UpdatePage(String gameID)
    {
        _instanceService.RefreshInstance(gameID);
    }

    public String GenerateHangmanGameID(Long guildID, Long channelID)
    {
        return "Hangman." + guildID + "." + channelID;
    }
    public String GenerateHangmanGameID(CommandInteraction interaction)
    { return GenerateHangmanGameID(interaction.getGuild().getIdLong(), interaction.getChannelIdLong()); }

    public MessageCreateBuilder SupplySetupScreen(GameInstance instance)
    {
        HangmanInstance hangmanInstance = (HangmanInstance) instance;
        StringSelectMenu.Builder selectionMenu = StringSelectMenu.create("hangman.select-category")
                .addOptions(GetCategoryOptions());

        if (hangmanInstance.category != null)
        {
            selectionMenu.setDefaultValues(hangmanInstance.category);
        }

        return new MessageCreateBuilder()
                .addEmbeds(new EmbedBuilder()
                        .setTitle("Hangman Setup")
                        .setDescription("Pick a word category!")
                        .setColor(Color.BLUE)
                        .build())
                .addActionRow(selectionMenu.build())
                .addActionRow(Button.of(ButtonStyle.PRIMARY, "hangman.start-game", "Start")
                                .withDisabled(hangmanInstance.category == null),
                        Button.of(ButtonStyle.DANGER, "hangman.cancel-game", "Cancel"));
    }

    public MessageCreateBuilder SupplyGame(GameInstance instance)
    {
        HangmanInstance hangmanInstance = (HangmanInstance) instance;
        return new MessageCreateBuilder()
                .addEmbeds(new EmbedBuilder()
                        .setTitle("Hangman Game: " + hangmanInstance.category)
                        .setDescription("```\n"+String.join("", hangmanInstance.word.chars().map(x ->
                                hangmanInstance.rightChars.contains((char)x) ? x :
                                        (char)x == ' ' ? ' ' : '_').mapToObj(Character::toString).toList()) + "\n"
                                + _config.hangmanStages[10-hangmanInstance.guessesRemaining]+"\nGuessed: "
                                + String.join(", ", hangmanInstance.wrongChars.stream().map(x -> x.toString()).toList())+"```")
                        .setColor(hangmanInstance.status.getColour())
                        .setFooter("Use /guess or /end-game")
                        .build());
    }

    public MessageCreateBuilder SupplyEndScreen(GameInstance instance)
    {
        HangmanInstance hangmanInstance = (HangmanInstance) instance;
        return new MessageCreateBuilder()
                .addEmbeds(new EmbedBuilder()
                        .setTitle(hangmanInstance.status.getTitle() + (hangmanInstance.status == HangmanStatus.Lost ? hangmanInstance.word : ""))
                        .setDescription((hangmanInstance.status == HangmanStatus.Lost ?
                                "```\n"+_config.hangmanStages[10] + "```\n" : "") +
                                "Would you like to play again?")
                        .setColor(hangmanInstance.status.getColour())
                        .build())
                .addActionRow(Button.of(ButtonStyle.PRIMARY, "hangman.playagain:yes", "Yes"),
                        Button.of(ButtonStyle.DANGER, "hangman.playagain:no", "No"));
    }

    public MessageCreateBuilder SupplyThankYouScreen(GameInstance instance)
    {
        return new MessageCreateBuilder()
                .addEmbeds(new EmbedBuilder()
                        .setTitle("Thanks for playing!")
                        .setDescription("It would be really appreciated if you took a moment to vote for and leave a " +
                                "review for the bot at: https://top.gg/bot/1218936835506573433")
                        .setFooter("Thread has been locked")
                        .setColor(Color.GRAY)
                        .build());
    }

    private List<SelectOption> GetCategoryOptions()
    {
        return Arrays.stream(_config.categories).map(x -> x.name).map(x -> SelectOption.of(x, x)).toList();
    }

    private String GetRandomWord(List<String> categoryWords)
    {
        return categoryWords.get(_random.nextInt(categoryWords.size())).toLowerCase();
    }
}
