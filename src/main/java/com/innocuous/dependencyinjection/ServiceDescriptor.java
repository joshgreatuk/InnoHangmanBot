package com.innocuous.dependencyinjection;

import java.util.Optional;
import java.util.function.Function;

class ServiceDescriptor
{
    public ServiceDescriptor(Class<?> referenceType, ServiceType type, Optional<Function<IServiceProvider, Object>> func, Optional<Object> value)
    {
        this.referenceType = referenceType;
        this.serviceType = type;
        this.valueFunc = func;
        this.value = value;
    }

    //TO-DO: Add Func support

    public Class<?> referenceType;
    public ServiceType serviceType;
    public Optional<Object> value;
    public Optional<Function<IServiceProvider, Object>> valueFunc;
}
