# Interaction Notes
## InteractionService
The InteractionService is responsible as a middle ground between interaction
modules and receiving JDA events, the InteractionService will be registered
as an event handler in the JDABuilder and will receive the events:
- onSlashCommandExecuted (Slash Commands)
- onMessageReceived (Text Commands)
- onCommandAutoCompleteInteraction (Autocompletes)
- onMessageContextInteraction (Message Context Commands)
- onUserContextInteraction (User Context Commands)
- onEntitySelectInteraction (Entity Select Menu Component)
- onStringSelectInteraction (String Select Menu Component)
- onButtonInteraction (Button Component)

## Modules
Modules are classes containing commands, they will be recognised by the
InteractionService via reflection and their commands will be added to the
command list for processing on execution. Modules should inherit from
the abstract class JDAModuleBase which will define the following methods:
- BeforeExecute
- AfterExecute

To store modules in the InteractionService, there will be a list of ModuleDefinitions

## Groups
An organisation annotation we can use is @Group, this will allow for
commands to be grouped and prefix's to be standard, this will have nesting
support, and its own preconditions, this means a whole group could
have a precondition through just one annotation.

Preconditions will still apply to classes without the @Group annotation and
the annotation is limited to twice in a module hierarchy, although creating
further nested module structures is still allowed

The allowed hierarchy is:
- Command
  - Subcommand (1x @Group)
  - SubcommandGroup 
    - Subcommand (2x @Group)

## Annotations
Commands will be registered through annotations of methods, the annotations
will include:
- @SlashCommand
- @TextCommand
- @UserContextCommand
- @MessageContextCommand

We can also register response commands from elements such as components,
they will include:
- @ButtonComponentCommand
- @StringSelectComponentCommand
- @EntitySelectComponentCommand

## Autocompletes
Some fields in command annotations support autocomplete, to implement this we
use the @Autocomplete base annotation, to add a new autocomplete method you
can extend the JDAAutocompleteBase class, which provides the abstract method
"String[] HandleAutocomplete(InteractionContext)" where only the first 25
elements of the array will be used

## Preconditions
Fields, Methods and Groups may have preconditions, this is just an annotation
that InteractionService will invoke and will return a result struct including
the IsSuccess Boolean and an Optional Exception for if it wasn't a success.
Preconditions should be invoked with a try catch to catch exceptions, and
an exception should be classed as a precondition failure

Permission-based preconditions will be handled through custom checking so
that we can filter with subcommands

## Registration
Module registration should be done before command registration to discord,
the order of execution between InteractionService and JDA should go:
1. (User) Initialize JDABuilderService
2. (User) Initialize InteractionService
3. (Auto) InteractionService registers as an event listener in its constructor
4. (User) Register modules with InteractionService
5. (User) Build JDA through JDABuilder
6. (Auto) JDA fired ready event
7. (Auto) InteractionService registers commands with JDA

## Execution
Due to the Dependency Injection required in the interaction system, there
is a dependency on com.innocuous.dependencyinjection

Upon receiving a command event the InteractionService takes the following steps 
to identify where to invoke such command, they are as follows:
1. Receive the command event from JDA
2. Query the command maps for the command reference string
3. Split the string using colons
4. Using the strings, navigate the module map to find the method name / func
5. Execute the method using try catch
6. Profit

Options for method execution:
1. Reflection, getMethod() and invoke
2. Consumer methods generated by registration

## InteractionConfig
Config values we will need for the interaction system include:
- Command Prefix
- Command Auto Register

## Speed (Indexing)
In the future we could use module indexing to index interactions so that
the InteractionService only needs to check a HashTable for a string entry
instead of searching through modules

## Type Converters
A type converter can turn any class used as a command parameter that wouldn't
usually be supported to be turned into a class that is supported. For example,
a base type converter is the EnumTypeConverter, since Enums wouldn't usually be
supported, the type converter converts the Enum to a String value and then
when given a String converts it back into an Enum to send as a parameter to
the command. Base type converters include:
- BaseTypeConverter<K, V>
- StringTypeConverter(BaseTypeConverter<String,V>)<V>
- NumberTypeConverter(BaseTypeConverter<Double,V>)<V>
- IntegerTypeConverter(BaseTypeConverter<Integer,V>)<V>
And implemented type converters include:
- EnumTypeConverter(StringTypeConverter<Enum>)
- FloatTypeConverter(NumberTypeConverter<Float>)