package com.innocuous.innohangmanbot.services.games;

import com.innocuous.dependencyinjection.servicedata.IInitializable;
import com.innocuous.dependencyinjection.servicedata.IStoppable;
import com.innocuous.innohangmanbot.services.InnoService;
import com.innocuous.innologger.ILogger;
import com.innocuous.innologger.LogMessage;
import com.innocuous.innologger.LogSeverity;
import net.bytebuddy.asm.Advice;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.apache.commons.collections4.Get;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import java.awt.Color;

public class GameInstanceService extends InnoService implements IInitializable, IStoppable
{
    public GameInstanceService(ILogger logger, GameInstanceServiceConfig config, GameInstanceData data)
    {
        super(logger);

        _config = config;
        _config.Init();

        _data = data;
        _data.Init();

        long delay = 1000L * 60L * _config.timeoutMinutes;
        long extraFive = 1000L * 60L * 5;
        _instanceTimeoutTimer = new Timer();
        _instanceTimeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                CheckTimeouts();
            }
        }, delay+extraFive, delay);
    }

    private final GameInstanceServiceConfig _config;
    private final GameInstanceData _data;
    private final Timer _instanceTimeoutTimer;
    private JDA jda;

    @Override
    public void Initialize(ReadyEvent readyEvent)
    {
        jda = readyEvent.getJDA();

        for (GameInstance instance : _data.instances.values())
        {
            RefreshInstance(instance.instanceID);
        }
    }

    @Override
    public void Shutdown()
    {
        _instanceTimeoutTimer.cancel();

        //Remove components and add Shutdown message to all instances
        for (GameInstance instance : _data.instances.values())
        {
            Message message = GetMessage(instance);
            if (message == null) continue;

            ArrayList<MessageEmbed> embeds = new ArrayList<>(message.getEmbeds());

            MessageEmbed embed = embeds.get(embeds.size()-1);
            EmbedBuilder builder = new EmbedBuilder();
            builder.copyFrom(embed);

            embeds.add(new EmbedBuilder()
                    .setTitle("InnoHangmanBot is offline")
                    .setColor(Color.RED)
                    .setDescription("Bot is offline, this instance is currently saved and will be resumed once the bot is back online")
                    .build());

            message.editMessage(new MessageEditBuilder()
                    .setComponents(Collections.emptyList())
                    .setEmbeds(embeds)
                    .build()).complete();
        }
    }

    public void RegisterInstance(GameInstance instance)
    {
        _data.instances.put(instance.instanceID, instance);
        RefreshInstance(instance.instanceID);
        _logger.Log(new LogMessage(this, "Registered game instance '" + instance.instanceID + "'", LogSeverity.Debug));
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

        MessageChannel channel = GetMessageChannel(instance);
        if (channel == null)
        {
            _logger.Log(new LogMessage(this, "Guild and/or Channel don't exist, closing instance '" + instanceID + "'", LogSeverity.Debug));
            CloseInstance(instanceID);
            return;
        }

        instance.lastInteracted = LocalDateTime.now();

        //If there is a message, grab it and update it using the supplier
        if (instance.currentMessageID != 0)
        {
            Message existingMessage = channel.retrieveMessageById(instance.currentMessageID).complete();
            if (_config.removeOldMessages)
            {
                channel.deleteMessageById(instance.currentMessageID).queue();
            }
            else
            {
                if (existingMessage != null) {
                    Message newMessage = existingMessage.editMessage(MessageEditData.fromCreateData(instance.messageSupplier.apply(instance).build())).complete();
                    instance.cachedMessage = newMessage;
                    return;
                }
            }
            instance.cachedMessage = null;
            instance.currentMessageID = 0L;
        }

        //If no message, create message using supplier
        Message message = channel.sendMessage(instance.messageSupplier.apply(instance).build()).complete();
        instance.currentMessageID = message.getIdLong();
        instance.cachedMessage = message;
    }


    public void SetInstanceSupplier(String instanceID, Function<GameInstance, MessageCreateBuilder> supplier, String supplierString)
    {
        if (!InstanceExists(instanceID))
        {
            _logger.Log(new LogMessage(this, "Requested instanceID '" + instanceID + "' doesn't exist", LogSeverity.Warning));
            return;
        }

        GameInstance instance = GetInstance(instanceID);
        instance.messageSupplier = supplier;
        instance.messageSupplierString = supplierString;
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

    public List<GameInstance> GetInstances()
    {
        return _data.instances.values().stream().toList();
    }

    public Boolean InstanceExists(String instanceID)
    {
        return _data.instances.containsKey(instanceID);
    }

    public int InstancesWithGuild(Long guildID)
    {
        if (guildID == 0) return 0;
        return _data.instances.values().stream().filter(x -> x.guildID != null && x.guildID.equals(guildID)).toList().size();
    }

    public int InstancesWithChannel(Long channelID)
    {
        if (channelID == 0) return 0;
        return _data.instances.values().stream().filter(x -> x.superChannelID != null && x.superChannelID.equals(channelID)).toList().size();
    }

    public MessageChannel GetMessageChannel(GameInstance instance)
    {
        if (instance == null) return null;

        MessageChannel channel;
        if (instance.guildID != 0)
        {
            Guild guild = jda.getGuildById(instance.guildID);
            if (guild == null) return null;
            channel = guild.getChannelById(GuildMessageChannel.class, instance.channelID);
        }
        else
        {
            channel = jda.getPrivateChannelById(instance.channelID);
        }

        return channel;
    }

    public Message GetMessage(GameInstance instance, GuildMessageChannel channel)
    {
        if (instance.cachedMessage != null) return instance.cachedMessage;
        if (channel == null) return null;

        return channel.getHistory().getMessageById(instance.currentMessageID);
    }
    public Message GetMessage(GameInstance instance)
    {
        if (instance.cachedMessage != null) return instance.cachedMessage;

        MessageChannel channel = GetMessageChannel(instance);
        if (channel == null || instance.currentMessageID == 0) return null;

        return channel.retrieveMessageById(instance.currentMessageID).complete();
    }

    private void CheckTimeouts()
    {
        _data.instances.values().stream()
                .filter(x -> x.lastInteracted.plusMinutes(_config.timeoutMinutes).isBefore(LocalDateTime.now()))
                .map(x -> x.instanceID)
                .forEach(y -> TimeoutInstance(y));

        _config.SaveFile(_config);
    }

    public void TimeoutInstance(String instanceID)
    {
        _logger.Log(new LogMessage(this, "Instance '" + instanceID + "' timed out"));
        SetInstanceSupplier(instanceID, this::SupplyTimeoutPage, "SupplyTimeoutPage");
        RefreshInstance(instanceID);
        //Get the channel, if it is a thread, lock and archive it
        GameInstance instance = GetInstance(instanceID);
        MessageChannel channel = GetMessageChannel(instance);
        if (channel.getType().isThread())
        {
            ThreadChannel thread = (ThreadChannel)channel;
            thread.getManager().setLocked(true).setArchived(true).queue();;
        }
        CloseInstance(instanceID);
    }

    private MessageCreateBuilder SupplyTimeoutPage(GameInstance instance)
    {
        return new MessageCreateBuilder()
                .addEmbeds(new EmbedBuilder()
                        .setTitle("Game Timed Out")
                        .setDescription("Game hasn't been interacted with in " + _config.timeoutMinutes + " minutes")
                        .build());
    }
}
