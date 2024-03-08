package com.innocuous.dependencyinjection;

import java.util.Arrays;
import java.util.List;

public interface IServiceProvider
{
    public <T> T GetService(Class<?> serviceClass);
    public List<Object> GetActiveServices();

    public <T> List<T> GetServicesWithInterface(Class<?> interfaceClass);

    public static Boolean HasInterface(Class<?> target, Class<?> interfaceClass)
    {
        return Arrays.stream(target.getInterfaces()).anyMatch(y -> y == interfaceClass)
                || Arrays.stream(target.getSuperclass().getInterfaces()).anyMatch(y -> y == interfaceClass);
    }
}
