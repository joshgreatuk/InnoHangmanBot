package com.innocuous.dependencyinjection;

import com.innocuous.dependencyinjection.logging.LogMessage;
import com.innocuous.dependencyinjection.servicedata.ServiceDescriptor;

import java.util.Hashtable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class ServiceCollection
{
    private Hashtable<Class<?>, ServiceDescriptor> _descriptors = new Hashtable<Class<?>, ServiceDescriptor>();
    private Optional<Class<?>> _logConsumerClass = Optional.empty();
    private Optional<String> _logConsumerMethod = Optional.empty();

    public ServiceProvider Build()
    {
        return new ServiceProvider(_descriptors, _logConsumerClass, _logConsumerMethod);
    }

    public ServiceCollection AddSingletonService(Class<?> serviceClass)
    { AddService(serviceClass, ServiceType.Singleton, Optional.empty(), Optional.empty()); return this; }
    public ServiceCollection AddSingletonService(Class<?> serviceClass, Function<IServiceProvider, Object> func)
    { AddService(serviceClass, ServiceType.Singleton, Optional.of(func), Optional.empty()); return this; }
    public ServiceCollection AddSingletonService(Class<?> serviceClass, Object value)
    { AddService(serviceClass, ServiceType.Singleton, Optional.empty(), Optional.of(value)); return this; }

    public ServiceCollection AddTransientService(Class<?> serviceClass)
    { AddService(serviceClass, ServiceType.Transient, Optional.empty(), Optional.empty()); return this; }
    public ServiceCollection AddTransientService(Class<?> serviceClass, Function<IServiceProvider, Object> func)
    { AddService(serviceClass, ServiceType.Transient, Optional.of(func), Optional.empty()); return this; }

    public ServiceCollection AddLogConsumer(Class<?> consumer, String method)
    { _logConsumerClass = Optional.of(consumer); _logConsumerMethod = Optional.of(method); return this; }

    private void AddService(Class<?> serviceClass, ServiceType serviceType, Optional<Function<IServiceProvider, Object>> func, Optional<Object> value)
    {
        if (_descriptors.containsKey(serviceClass)) _descriptors.remove(serviceClass);
        _descriptors.put(serviceClass, new ServiceDescriptor(serviceClass, serviceType, func, value));
    }

}
