package com.innocuous.dependencyinjection.tests;

import com.innocuous.dependencyinjection.IServiceProvider;

public class SingletonServiceA
{
    public IServiceProvider services;
    public SingletonServiceB dependency;

    public SingletonServiceA(IServiceProvider services, SingletonServiceB dependency)
    {
        this.services = services;
        this.dependency = dependency;
    }
}
