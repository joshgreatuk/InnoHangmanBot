package com.innocuous.innohangmanbot.modules;

import ch.qos.logback.core.Layout;
import com.innocuous.jdamodulesystem.JDAModuleBase;
import com.innocuous.jdamodulesystem.annotations.SlashCommand;
import com.innocuous.jdamodulesystem.annotations.components.ButtonComponent;
import com.innocuous.jdamodulesystem.annotations.components.EntitySelectComponent;
import com.innocuous.jdamodulesystem.annotations.components.StringSelectComponent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.internal.entities.MemberImpl;

import javax.swing.text.html.parser.Entity;
import java.util.Arrays;
import java.util.List;

public class InfoModule extends JDAModuleBase
{
    @SlashCommand(name = "info", description = "Get info about InnoHangmanBot")
    public void Info()
    {
        String infoDescription = "**Author:** innocuousuk\n" +
                        "**Version:** 1.1\n" +
                        "**Top.gg Link:** https://top.gg/bot/1218936835506573433";

        commandInteraction.reply(new MessageCreateBuilder()
                        .addEmbeds(new EmbedBuilder()
                                .setTitle("InnoHangmanBot Info")
                                .setDescription(infoDescription)
                                .build())
                        .build())
                .setEphemeral(true).queue();
    }

    private StringSelectMenu stringSelectMenu;
    private EntitySelectMenu entitySelectMenu;

//    @SlashCommand(name = "component-menu", description = "Open a component menu")
//    public void ComponentMenu()
//    {
//        commandInteraction.reply(new MessageCreateBuilder()
//                        .addEmbeds(new EmbedBuilder().setTitle("Component Menu").build())
//                        .addActionRow(Button.of(ButtonStyle.DANGER, "close-button", "Close"))
//                        .addActionRow(StringSelectMenu.create("select-string").addOptions(GetStringOptions()).setMaxValues(1).build())
//                        .addActionRow(EntitySelectMenu.create("select-entity", EntitySelectMenu.SelectTarget.USER).setMaxValues(1).build())
//                .build()).queue();
//    }
//
//    @ButtonComponent(customID = "close-button")
//    public void CloseButton()
//    {
//        List<LayoutComponent> components = componentInteraction.getMessage().getComponents();
//
//        componentInteraction.editMessage("Closed").queue();
//        componentInteraction.getMessageChannel().deleteMessageById(componentInteraction.getMessageId()).queue();
//    }
//
//    @StringSelectComponent(customID = "select-string")
//    public void StringSelect(List<String> selected)
//    {
//        List<LayoutComponent> components = componentInteraction.getMessage().getComponents();
//        components.forEach(x -> x.updateComponent(componentInteraction.getComponentId(),
//                StringSelectMenu.create(componentInteraction.getComponentId())
//                        .addOptions(GetStringOptions())
//                        .setDefaultOptions(selected.stream().map(y -> SelectOption.of(y, y)).toList())
//                        .build()));
//        componentInteraction.editMessage(new MessageEditBuilder()
//                .setComponents(components)
//                .build()).queue();
//    }
//
//    @EntitySelectComponent(customID = "select-entity")
//    public void EntitySelect(List<MemberImpl> selected)
//    {
//        List<LayoutComponent> components = componentInteraction.getMessage().getComponents();
//        components.forEach(x -> x.updateComponent(componentInteraction.getComponentId(),
//                EntitySelectMenu.create(componentInteraction.getComponentId(), EntitySelectMenu.SelectTarget.USER)
//                        .setDefaultValues(selected.stream().map(y -> EntitySelectMenu.DefaultValue.from(y.getUser())).toList())
//                        .build()));
//
//        componentInteraction.editMessage(new MessageEditBuilder()
//                .setComponents(components)
//                .build()).queue();
//    }
//
//    private List<SelectOption> GetStringOptions()
//    {
//        return Arrays.stream(new String[]
//                {
//                    "Option A", "Option B", "Option C"
//                }).map(x -> SelectOption.of(x, x)).toList();
//    }
}
