package com.innocuous.datasystem;

import java.util.HashMap;

public abstract class Database<KeyType, ValueType> extends HashMap<KeyType, ValueType>  implements IDataService
{

}
