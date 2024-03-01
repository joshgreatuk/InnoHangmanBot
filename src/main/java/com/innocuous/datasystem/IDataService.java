package com.innocuous.datasystem;

/**
 * For splitting DataService and possible RemoteDataService
 *
 * Databases are registered by instance and loaded in the same method
 * only one data provider is allowed for DataService instance
 * data is injected straight into the Database abstract class, as it is a HashMap
 * any setter methods in the Database should link back to IDataProvider via a runtime reference
 */
public interface IDataService
{

}
