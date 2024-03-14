package com.innocuous.innohangmanbot.modules;

import com.innocuous.jdamodulesystem.JDAModuleBase;
import com.innocuous.jdamodulesystem.annotations.Group;
import com.innocuous.jdamodulesystem.annotations.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.Optional;

public class InfoModule extends JDAModuleBase
{
    @SlashCommand(name = "info", description = "Get info about InnoHangmanBot")
    public void Info()
    {
        String infoDescription = "**Author:** innocuousuk\n" +
                        "**Version:** 1.0";

        interaction.reply(new MessageCreateBuilder()
                        .addEmbeds(new EmbedBuilder()
                                .setTitle("InnoHangmanBot Info")
                                .setDescription(infoDescription)
                                .build())
                        .build())
                .setEphemeral(true).queue();
    }
}
