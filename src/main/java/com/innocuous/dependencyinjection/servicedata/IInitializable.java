package com.innocuous.dependencyinjection.servicedata;

import net.dv8tion.jda.api.events.session.ReadyEvent;

public interface IInitializable
{
    public default void Initialize() {};
    public default void Initialize(ReadyEvent readyEvent) {};
}
