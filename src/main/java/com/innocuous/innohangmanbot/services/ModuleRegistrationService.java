package com.innocuous.innohangmanbot.services;

import com.innocuous.dependencyinjection.servicedata.IInitializable;
import com.innocuous.innohangmanbot.modules.InfoModule;
import com.innocuous.innologger.ILogger;
import com.innocuous.jdamodulesystem.InteractionService;
import net.dv8tion.jda.api.events.session.ReadyEvent;

import java.util.List;

public class ModuleRegistrationService extends InnoService implements IInitializable
{
    private final InteractionService _interactionService;

    public ModuleRegistrationService(ILogger logger, InteractionService interactionService)
    {
        super(logger);
        _interactionService = interactionService;
    }

    private final Class<?>[] moduleClases = new Class<?>[]
            {
                InfoModule.class
            };

    @Override
    public void Initialize(ReadyEvent readyEvent)
    {
        _interactionService.setJDA(readyEvent.getJDA());
        _interactionService.AddModules(List.of(moduleClases));
        _interactionService.RegisterModulesToGuild(0L); //Auto register fires before this, so register manually
    }
}
