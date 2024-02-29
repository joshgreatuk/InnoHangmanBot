package com.innocuous.dependencyinjection;

import com.innocuous.dependencyinjection.servicedata.ServiceDescriptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

class ServiceProvider implements IServiceProvider
{
    public ServiceProvider(Hashtable<Class<?>, ServiceDescriptor> descriptors)
    {
        _descriptors = descriptors;
    }

    private final Hashtable<Class<?>, ServiceDescriptor> _descriptors;

    @Override
    public <T> T GetService(Class<?> serviceClass)
    {
        if (!_descriptors.containsKey(serviceClass)) return null;

        if (serviceClass == IServiceProvider.class || serviceClass == ServiceProvider.class) return (T)this;

        ServiceDescriptor descriptor = _descriptors.get(serviceClass);
        if (descriptor.serviceType == ServiceType.Transient)
            return descriptor.valueFunc.isEmpty()
                    ? (T)InstantiateService((serviceClass))
                    : (T)descriptor.valueFunc.get().apply(this);

        //serviceType must be Singleton
        if (descriptor.value.isEmpty())
            descriptor.value = descriptor.valueFunc.isEmpty()
                    ? Optional.of(InstantiateService(serviceClass))
                    : Optional.of(descriptor.valueFunc.get().apply(this));
        return (T)descriptor.value.get();
    }

    @Override
    public List<Object> GetActiveServices()
    {
        return _descriptors.values().stream()
                .filter(x -> x.value.isPresent())
                .map(x -> x.value.get())
                .collect(Collectors.toList());
    }

    private Object InstantiateService(Class<?> serviceClass)
    {
        Constructor[] constructors = serviceClass.getDeclaredConstructors();

        //Take the first constructor
        Constructor constructor = constructors[0];
        Object[] params = new Object[constructor.getParameterCount()];
        Class<?>[] paramTypes = constructor.getParameterTypes();

        for (int i=0; i < paramTypes.length; i++)
        {
            params[i] = GetService(paramTypes[i]);
        }

        try
        {
            return constructor.newInstance(params);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
