package com.innocuous.dependencyinjection;

import com.innocuous.dependencyinjection.exceptions.ServiceNotFoundException;

public interface IServiceProvider
{
    public <T> T GetService(Class<?> serviceClass);
}
