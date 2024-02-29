package com.innocuous.dependencyinjection;

import java.util.List;

public interface IServiceProvider
{
    public <T> T GetService(Class<?> serviceClass);
    public List<Object> GetActiveServices();

    <T> List<T> GetServicesWithInterface(Class<?> interfaceClass);
}
