package com.innocuous.jdamodulesystem.modules;

import com.innocuous.jdamodulesystem.JDAModuleBase;
import com.innocuous.jdamodulesystem.annotations.Description;
import com.innocuous.jdamodulesystem.annotations.Group;
import com.innocuous.jdamodulesystem.annotations.SlashCommand;
import net.dv8tion.jda.api.entities.User;

import java.util.Optional;

@Group(name = "Test")
public class TestModule extends JDAModuleBase
{
    @Group(name = "SlashCommand")
    public class SlashCommandModule extends JDAModuleBase
    {
        public class TestNestedModule extends JDAModuleBase
        {
            @SlashCommand(name = "cool-command")
            public void CoolCommand(String stringVal, Integer intVal, Boolean boolVal)
            {

            }
        }

        @SlashCommand(name = "set-nickname")
        public void SetNickname(@Description(description = "The target user") User user, Optional<String> nickname)
        {

        }
    }

    @SlashCommand(name = "ping", description = "Ping the bot!")
    public void Ping()
    {

    }
}
