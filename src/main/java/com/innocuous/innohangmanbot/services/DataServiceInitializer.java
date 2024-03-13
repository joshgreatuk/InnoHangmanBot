package com.innocuous.innohangmanbot.services;

import com.innocuous.datasystem.IDataService;
import com.innocuous.datasystem.IDatabase;
import com.innocuous.dependencyinjection.IServiceProvider;
import com.innocuous.dependencyinjection.servicedata.IInitializable;
import com.innocuous.innologger.ILogger;
import com.innocuous.innologger.InnoLoggerService;

import java.util.List;

/**
 * This class is for grabbing IDatabase and IDataProvider instances in the IServiceProvider
 * and registering them with the
 */
public class DataServiceInitializer extends InnoService implements IInitializable
{
    public DataServiceInitializer(ILogger logger, IDataService dataService, IServiceProvider services)
    {
        super(logger);
        _dataService = dataService;
        _services = services;
    }

    private final IDataService _dataService;
    private final IServiceProvider _services;

    @Override
    public void Initialize()
    {
        List<IDatabase> databases = _services.<IDatabase>GetServicesWithInterface(IDatabase.class);
        //databases.forEach(x -> _dataService);
    }
}
