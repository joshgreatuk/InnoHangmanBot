package com.innocuous.dependencyinjection;

import java.util.List;

public interface IServiceProvider
{
    public <T> T GetService(Class<?> serviceClass);
    public List<Object> GetActiveServices();

    public <T> List<T> GetServicesWithInterface(Class<?> interfaceClass);
}
