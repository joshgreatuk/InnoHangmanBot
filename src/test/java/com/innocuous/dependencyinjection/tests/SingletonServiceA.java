package com.innocuous.dependencyinjection.tests;

public class SingletonServiceA
{
    public SingletonServiceB dependency;

    public SingletonServiceA(SingletonServiceB dependency)
    {
        this.dependency = dependency;
    }
}
