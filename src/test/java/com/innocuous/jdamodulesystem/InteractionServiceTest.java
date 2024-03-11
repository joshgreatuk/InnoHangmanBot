package com.innocuous.jdamodulesystem;

import com.innocuous.dependencyinjection.IServiceProvider;
import com.innocuous.dependencyinjection.ServiceCollection;
import com.innocuous.innologger.ILogger;
import com.innocuous.innologger.LogMessage;
import com.innocuous.jdamodulesystem.modules.TestModule;
import net.dv8tion.jda.api.JDABuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InteractionServiceTest
{
    private static IServiceProvider services;

    @Test
    void addModules()
    {
        services = new ServiceCollection()
                .AddSingletonService(JDABuilder.class, x -> JDABuilder.createDefault(""))
                .AddSingletonService(InteractionService.class)
                .Build();
        InteractionService interactionService = services.GetService(InteractionService.class);

        interactionService.AddModule(TestModule.class);

        services.<ILogger>GetService(ILogger.class).Log(new LogMessage(this, "Yuh!"));
    }
}