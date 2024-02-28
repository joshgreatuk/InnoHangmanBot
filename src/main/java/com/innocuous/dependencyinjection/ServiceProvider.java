package com.innocuous.dependencyinjection;

import kotlin.NotImplementedError;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Optional;
import java.util.stream.Collectors;

class ServiceProvider implements IServiceProvider
{
    public ServiceProvider(Hashtable<Class<?>, ServiceDescriptor> descriptors)
    {
        _descriptors = descriptors;
    }

    private final Hashtable<Class<?>, ServiceDescriptor> _descriptors;

    public <T> T GetService(Class<?> serviceClass)
    {
        if (!_descriptors.containsKey(serviceClass)) return null;

        ServiceDescriptor descriptor = _descriptors.get(serviceClass);
        if (descriptor.serviceType == ServiceType.Transient)
            return descriptor.valueFunc.isEmpty()
                    ? (T)InstantiateService((serviceClass))
                    : (T)descriptor.valueFunc.get().apply(this);

        //serviceType must be Singleton
        if (descriptor.value.isEmpty())
            descriptor.value = descriptor.valueFunc.isEmpty()
                    ? Optional.of((T)InstantiateService(serviceClass))
                    : Optional.of((T)descriptor.valueFunc.get().apply(this));
        return (T)descriptor.value.get();
    }

    public Object[] GetActiveServices()
    {
        ArrayList<Object> activeServices = new ArrayList<Object>();
        activeServices.addAll(_descriptors.values().stream()
                .filter(x -> x.value.isPresent())
                .map(x -> x.value.get())
                .toList());
        return activeServices.toArray();
    }

    private <T> T InstantiateService(Class<?> serviceClass)
    {
        Constructor[] constructors = serviceClass.getDeclaredConstructors();

        //Take the first constructor
        Constructor constructor = constructors[0];
        Object[] params = new Object[constructor.getParameterCount()];
        Class<?>[] paramTypes = constructor.getParameterTypes();

        for (int i=0; i < paramTypes.length; i++)
        {
            params[i] = (Object)GetService(paramTypes[i]);
        }

        try
        {
            return (T)constructor.newInstance(params);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
