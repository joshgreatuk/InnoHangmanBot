package com.innocuous.datasystem;

public interface IDatabase
{
    public String GetDatabasePath();
    public Boolean IsReadOnly();

    public Class<?> GetKeyType();
    public Class<?> GetValueType();
}
