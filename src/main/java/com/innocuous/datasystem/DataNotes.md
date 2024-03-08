## Databases
In some cases the database could be loaded into and modified at runtime, such as
a JSON database while some other databases should be handled by a third party
such as an SQL database, in this case the Database object should just be a
builder for an SQL pattern.

## IDataProvider
A data provider should take the target classes and an SQL query and respond
to that query with either an error or a result

## Queries
