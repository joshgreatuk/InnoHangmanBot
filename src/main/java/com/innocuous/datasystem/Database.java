package com.innocuous.datasystem;

import java.util.HashMap;

public abstract class Database<KeyType, ValueType> extends HashMap<KeyType, ValueType>  implements IDataService
{
    @Override
    public ValueType put(KeyType key, ValueType value)
    {
        //Change value through IServiceProvider
        ValueType oldValue = null;
        if (containsKey(key)) oldValue = get(key);
        super.put(key, value);

        return oldValue;
    }
}
