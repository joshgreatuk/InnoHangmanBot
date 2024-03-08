package com.innocuous.dependencyinjection;

import com.innocuous.dependencyinjection.servicedata.ServiceDescriptor;
import com.innocuous.dependencyinjection.tests.*;
import org.junit.jupiter.api.*;
import org.opentest4j.MultipleFailuresError;

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServiceCollectionProviderTest
{
    private static ServiceCollection collection;
    private static Field collectionDescriptors;

    private static ServiceProvider services;
    private static Field servicesDescriptors;

    @Test
    @Order(0)
    void serviceCollectionConstuctor() throws NoSuchFieldException, IllegalAccessException
    {
        collection = new ServiceCollection();
        collectionDescriptors = collection.getClass().getDeclaredField("_descriptors");
        collectionDescriptors.setAccessible(true);
        assertNotNull(collectionDescriptors.get(collection));
    }

    @Test
    @Order(1)
    void addSingletonService() throws IllegalAccessException
    {
        collection.AddSingletonService(SingletonServiceA.class);
        collection.AddSingletonService(SingletonServiceB.class, x -> new SingletonServiceB());
        SingletonTestService testService = new SingletonTestService();
        collection.AddSingletonService(SingletonTestService.class, testService);


        assertEquals(3, ((Hashtable<Class<?>, ServiceDescriptor>)collectionDescriptors.get(collection))
                .size());
        assertTrue(((Hashtable<Class<?>, ServiceDescriptor>)collectionDescriptors.get(collection))
                .get(SingletonTestService.class).value.isPresent());

        collection.AddSingletonService(SingletonSubService.class);
    }

    @Test
    @Order(2)
    void addTransientService() throws IllegalAccessException
    {
        collection.AddTransientService(TransientService.class);
        collection.AddTransientService(TransientTestService.class, x -> new TransientTestService());

        assertEquals(6, ((Hashtable<Class<?>, ServiceDescriptor>)collectionDescriptors.get(collection))
                .size());
    }

    @Test
    @Order(3)
    void build() throws IllegalAccessException, NoSuchFieldException
    {
        //Ensure ServiceProvider is built correctly
        services = collection.Build();

        servicesDescriptors = services.getClass().getDeclaredField("_descriptors");
        servicesDescriptors.setAccessible(true);
        assertSame(collectionDescriptors.get(collection), servicesDescriptors.get(services));
    }

    @Test
    @Order(5)
    void getService() throws MultipleFailuresError, IllegalAccessException, NoSuchFieldException
    {
        Hashtable<Class<?>, ServiceDescriptor> currentDescriptors =
                (Hashtable<Class<?>, ServiceDescriptor>) servicesDescriptors.get(services);

        assertAll(
                () -> assertTrue(currentDescriptors.get(SingletonServiceA.class).value.isEmpty()),
                () -> assertTrue(currentDescriptors.get(SingletonServiceB.class).value.isEmpty()),
                () -> assertTrue(currentDescriptors.get(SingletonTestService.class).value.isPresent()),
                () -> assertTrue(currentDescriptors.get(TransientService.class).value.isEmpty()),
                () -> assertTrue(currentDescriptors.get(TransientTestService.class).value.isEmpty())
        );

        assertAll(
                () -> assertNotNull(services.GetService(SingletonServiceA.class)),
                () -> assertNotNull(services.GetService(SingletonServiceB.class)),
                () -> assertNotNull(services.GetService(SingletonTestService.class)),
                () -> assertNotNull(services.GetService(TransientService.class)),
                () -> assertNotNull(services.GetService(TransientService.class))
        );

        assertAll(
                () -> assertTrue(currentDescriptors.get(SingletonServiceA.class).value.isPresent()),
                () -> assertNotNull(((SingletonServiceA)currentDescriptors.get(SingletonServiceA.class).value.get()).services),
                () -> assertNotNull(((SingletonServiceA)currentDescriptors.get(SingletonServiceA.class).value.get()).dependency),
                () -> assertTrue(currentDescriptors.get(SingletonServiceB.class).value.isPresent()),
                () -> assertTrue(currentDescriptors.get(SingletonTestService.class).value.isPresent()),
                () -> assertTrue(currentDescriptors.get(TransientService.class).value.isEmpty()),
                () -> assertTrue(currentDescriptors.get(TransientTestService.class).value.isEmpty())
        );
    }

    @Test
    @Order(5)
    void getSubclassService()
    {
        //Only the SingletonSubClass is here, so it should return SingletonSubClass as SingletonSuperClass
        assertNotNull(services.GetService(SingletonSuperService.class));
    }

    @Test
    @Order(4)
    void getActiveServices()
    {
        List<Object> activeServices = services.GetActiveServices();
        assertEquals(1, activeServices.size()); //SingletonTestService
    }

    @Test
    @Order(5)
    void getServicesWithInterface()
    {
        List<Object> interfacedServices = services.GetServicesWithInterface(ITest.class);
        assertEquals(3, interfacedServices.size());
        assertTrue(interfacedServices.stream().anyMatch(x -> x.getClass() == SingletonSubService.class));
    }
}