package com.innocuous.dependencyinjection;

import com.innocuous.dependencyinjection.servicedata.ServiceDescriptor;
import com.innocuous.innologger.ConsoleLogger;
import com.innocuous.innologger.ILogger;
import com.innocuous.innologger.LogMessage;
import com.innocuous.innologger.LogSeverity;
import org.apache.commons.collections4.Get;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


class ServiceProvider implements IServiceProvider
{
    public ServiceProvider(Hashtable<Class<?>, ServiceDescriptor> descriptors)
    {
        _descriptors = descriptors;

        Optional<ILogger> logger = this.<ILogger>GetServicesWithInterface(ILogger.class).stream().findFirst();
        if (logger.isEmpty())
        {
            logger = Optional.of(new ConsoleLogger());
            logger.get().Log(new LogMessage(this, "No ILogger service found, using fallback logger of type '" + ConsoleLogger.class.getName() + "'"));
        }
        _logger = logger.get();

        _logger.Log(new LogMessage(this, "Descriptors and logger initialized"));
    }

    private final Hashtable<Class<?>, ServiceDescriptor> _descriptors;
    private final ILogger _logger;

    @Override
    public <T> T GetService(Class<?> serviceClass)
    {
        if (serviceClass == IServiceProvider.class || serviceClass == ServiceProvider.class) return (T)this;

        if (!_descriptors.containsKey(serviceClass))
        {
            if (serviceClass.isInterface())
            {
                Class<?> finalServiceClass = serviceClass;
                Optional<ServiceDescriptor> interfacedClass = _descriptors.values().stream().findFirst()
                        .filter(x -> Arrays.stream(x.referenceType.getInterfaces()).anyMatch(y -> y == finalServiceClass)
                        || Arrays.stream(x.referenceType.getSuperclass().getInterfaces()).anyMatch(y -> y == finalServiceClass));

                if (interfacedClass.isPresent())
                {
                    Log(new LogMessage(this, "GetService '" + serviceClass.getName() + "' not found, using superclass '" + interfacedClass.get().referenceType.getName() + "'", LogSeverity.Debug));
                    serviceClass = interfacedClass.get().referenceType;
                }
            }
            else
            {
                Class<?> finalServiceClass = serviceClass;
                Optional<ServiceDescriptor> subClassService = _descriptors.values().stream().filter(x -> x.referenceType.getSuperclass() == finalServiceClass).findFirst();
                if (subClassService.isPresent()) {
                    Log(new LogMessage(this, "GetService '" + serviceClass.getName() + "' not found, using subclass '" + subClassService.get().referenceType.getName() + "'", LogSeverity.Debug));
                    serviceClass = subClassService.get().referenceType;
                }
            }

            if (!_descriptors.containsKey(serviceClass))
            {
                Log(new LogMessage(this, "GetService '" + serviceClass.getName() + "' not found", LogSeverity.Warning));
                return null;
            }
        }

        ServiceDescriptor descriptor = _descriptors.get(serviceClass);
        if (descriptor.serviceType == ServiceType.Transient)
        {
            T value =  descriptor.valueFunc.isEmpty()
                    ? (T) InstantiateService((serviceClass))
                    : (T) descriptor.valueFunc.get().apply(this);
            Log(new LogMessage(this, "GetService returned Transient service '" + serviceClass.getName() + "'", LogSeverity.Verbose));
            return value;
        }

        //serviceType must be Singleton
        if (descriptor.value.isEmpty())
        {
            descriptor.value = descriptor.valueFunc.isEmpty()
                    ? Optional.of(InstantiateService(serviceClass))
                    : Optional.of(descriptor.valueFunc.get().apply(this));
        }
        Log(new LogMessage(this, "GetService returned Singleton service '" + serviceClass.getName() + "'", LogSeverity.Verbose));
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

    @Override
    public <T> List<T> GetServicesWithInterface(Class<?> interfaceClass)
    {
        return _descriptors.values().stream()
                .filter(x -> Arrays.stream(x.referenceType.getInterfaces()).anyMatch(y -> y == interfaceClass)
                || (x.referenceType.getSuperclass() != null
                        && Arrays.stream(x.referenceType.getSuperclass().getInterfaces()).anyMatch(y -> y == interfaceClass)))
                .map(x -> (T)GetService(x.referenceType))
                .collect(Collectors.toList());
    }

    private Object InstantiateService(Class<?> serviceClass)
    {
        Constructor[] constructors = serviceClass.getDeclaredConstructors();

        //Take the first constructor
        Constructor constructor = constructors[0];
        Log(new LogMessage(this, "Using constructor '" + constructor.toString() + "'", LogSeverity.Debug));
        Object[] params = new Object[constructor.getParameterCount()];
        Class<?>[] paramTypes = constructor.getParameterTypes();

        for (int i=0; i < paramTypes.length; i++)
        {
            params[i] = GetService(paramTypes[i]);
            Log(new LogMessage(this, "Param type '" + paramTypes[i].getName() + "' for '" + serviceClass.getName() + "' value is '" + (params[i] == null ? "null" : params[i].toString()) + "'", LogSeverity.Verbose));
            if (params[i] == null) _logger.Log(new LogMessage(this, "Param of type '" + paramTypes[i].getName() + "' for '" + serviceClass.getName() + "'s constructor value is null", LogSeverity.Warning));
        }

        try
        {
            Log(new LogMessage(this, "Instantiated service '" + serviceClass.getName() + "' with '" + params.length + "' params", LogSeverity.Debug));
            return constructor.newInstance(params);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public void Log(LogMessage message)
    {
        if (_logger == null) return;
        _logger.Log(message);
    }
}
