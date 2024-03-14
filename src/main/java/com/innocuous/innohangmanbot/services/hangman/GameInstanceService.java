package com.innocuous.innohangmanbot.services.hangman;

import com.innocuous.dependencyinjection.servicedata.IInitializable;
import com.innocuous.dependencyinjection.servicedata.IStoppable;
import com.innocuous.innohangmanbot.services.InnoService;
import com.innocuous.innologger.ILogger;
import com.innocuous.innologger.LogMessage;
import com.innocuous.innologger.LogSeverity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.apache.commons.collections4.Get;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;

public class GameInstanceService extends InnoService implements IInitializable, IStoppable
{
    public GameInstanceService(ILogger logger, GameInstanceServiceConfig config, GameInstanceData data)
    {
        super(logger);

        _config = config;
        _config.Init();

        _data = data;
        _data.Init();
    }

    private final GameInstanceServiceConfig _config;
    private final GameInstanceData _data;
    private JDA jda;

    @Override
    public void Initialize(ReadyEvent readyEvent)
    {
        jda = readyEvent.getJDA();

        //Validate guild, channel and messages still exist


        //Rebuild message to exclude shutdown message

    }

    @Override
    public void Shutdown()
    {
        //Remove components and add Shutdown message to all instances
        ArrayList<MessageEmbed> embeds = new ArrayList<>(message.getEmbeds());
        embeds.add(new EmbedBuilder()
                .setTitle("InnoHangmanBot is offline")
                .setColor(Color.red)
                .setDescription("Bot is offline, this instance is currently saved and will be resumed once the bot is back online")
                .build());
    }

    public void RegisterInstance(GameInstance instance)
    {

    }

    public void CloseInstance(String instanceID)
    {
        //Remove instance from list, if messageExists, remove components and display closed footer
        GameInstance instance = GetInstance(instanceID);
        if (instance == null) return;

        _data.instances.remove(instanceID);
        _logger.Log(new LogMessage(this, "Instance '" + instanceID + "' closed", LogSeverity.Verbose));

        Message message = GetMessage(instance);
        if (message == null) return;

        ArrayList<MessageEmbed> embeds = new ArrayList<>(message.getEmbeds());
        if (!embeds.isEmpty())
        {
            MessageEmbed embed = embeds.get(embeds.size()-1);
            EmbedBuilder builder = new EmbedBuilder();
            builder.copyFrom(embed);
            builder.setFooter("This instance is closed, it is no longer interactable");
            embeds.set(embeds.size()-1, builder.build());
        }
        else
        {
            embeds.add(new EmbedBuilder()
                            .setDescription("This instance is closed, it is no longer interactable")
                    .build());
        }

        message.editMessage(new MessageEditBuilder()
                        .setComponents(Collections.emptyList())
                        .setEmbeds(embeds)
                        .setReplace(true)
                .build()).queue();
    }

    public void RefreshInstance(String instanceID)
    {
        //Validate guild, channel and messages still exist
        GameInstance instance = GetInstance(instanceID);
        if (instance == null) return;

        if (instance.messageSupplier == null)
        {
            _logger.Log(new LogMessage(this, "Instance '" + instanceID + "' messageSupplier is null", LogSeverity.Error));
            return;
        }

        boolean hasMessage = instance.currentMessageID != 0;
        GuildMessageChannel channel = GetMessageChannel(instance);
        if (channel == null)
        {
            _logger.Log(new LogMessage(this, "Guild and/or Channel don't exist, closing instance '" + instanceID + "'", LogSeverity.Debug));
            CloseInstance(instanceID);
            return;
        }

        //If there is a message, grab it and update it using the supplier
        if (instance.currentMessageID != 0)
        {
            Message existingMessage = channel.getHistory().getMessageById(instance.currentMessageID);
            if (existingMessage != null)
            {
                existingMessage.editMessage(MessageEditData.fromCreateData(instance.messageSupplier.get().build())).queue();
                return;
            }

            instance.currentMessageID = 0L;
        }

        //If no message, create message using supplier
        channel.sendMessage(instance.messageSupplier.get().build()).onSuccess(
                x -> instance.currentMessageID = x.getIdLong()).queue();
    }


    public void SetInstanceSupplier(String instanceID, Supplier<MessageCreateBuilder> supplier)
    {
        if (!InstanceExists(instanceID))
        {
            _logger.Log(new LogMessage(this, "Requested instanceID '" + instanceID + "' doesn't exist", LogSeverity.Warning));
            return;
        }

        GetInstance(instanceID).messageSupplier = supplier;
    }

    public GameInstance GetInstance(String instanceID)
    {
        if (!InstanceExists(instanceID))
        {
            _logger.Log(new LogMessage(this, "Requested instanceID '" + instanceID + "' doesn't exist", LogSeverity.Warning));
            return null;
        }

        GameInstance instance = _data.instances.get(instanceID);
        return instance;
    }

    public Boolean InstanceExists(String instanceID)
    {
        return _data.instances.containsKey(instanceID);
    }

    public GuildMessageChannel GetMessageChannel(GameInstance instance)
    {
        if (instance == null) return null;

        Guild guild = jda.getGuildById(instance.guildID);
        if (guild == null) return null;

        GuildMessageChannel channel = guild.getChannelById(GuildMessageChannel.class, instance.channelID);
        if (channel == null) return null;

        return channel;
    }

    public Message GetMessage(GameInstance instance, GuildMessageChannel channel)
    {
        if (channel == null) return null;

        return channel.getHistory().getMessageById(instance.currentMessageID);
    }
    public Message GetMessage(GameInstance instance)
    {
        GuildMessageChannel channel = GetMessageChannel(instance);
        if (channel == null) return null;

        return channel.getHistory().getMessageById(instance.currentMessageID);
    }
}
