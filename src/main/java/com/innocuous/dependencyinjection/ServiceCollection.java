package com.innocuous.dependencyinjection;

import javax.swing.text.html.Option;
import java.util.Hashtable;
import java.util.Optional;
import java.util.function.Function;

public class ServiceCollection
{
    private Hashtable<Class<?>, ServiceDescriptor> _descriptors = new Hashtable<Class<?>, ServiceDescriptor>();

    public ServiceProvider Build()
    {
        return new ServiceProvider(_descriptors);
    }

    public void AddSingletonService(Class<?> serviceClass)
    { AddService(serviceClass, ServiceType.Singleton, Optional.empty(), Optional.empty()); }
    public void AddSingletonService(Class<?> serviceClass, Function<IServiceProvider, Object> func)
    { AddService(serviceClass, ServiceType.Singleton, Optional.of(func), Optional.empty()); }
    public void AddSingletonService(Class<?> serviceClass, Object value)
    { AddService(serviceClass, ServiceType.Singleton, Optional.empty(), Optional.of(value)); }

    public void AddTransientService(Class<?> serviceClass)
    { AddService(serviceClass, ServiceType.Transient, Optional.empty(), Optional.empty()); }
    public void AddTransientService(Class<?> serviceClass, Function<IServiceProvider, Object> func)
    { AddService(serviceClass, ServiceType.Transient, Optional.of(func), Optional.empty()); }


    private void AddService(Class<?> serviceClass, ServiceType serviceType, Optional<Function<IServiceProvider, Object>> func, Optional<Object> value)
    {
        if (_descriptors.containsKey(serviceClass)) _descriptors.remove(serviceClass);
        _descriptors.put(serviceClass, new ServiceDescriptor(serviceClass, serviceType, func, value));
    }
}
