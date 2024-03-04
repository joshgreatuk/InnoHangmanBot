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

## Registration
Module registration should be done before command registration to discord,
the order of execution between InteractionService and JDA should go:
1. (User) Initialize JDABuilder
2. (User) Initialize InteractionService
3. (Auto) InteractionService registers as an event listener in its constructor
4. (User) Register modules with InteractionService
5. (User) Build JDA through JDABuilder
6. (Auto) JDA fired ready event
7. (Auto) InteractionService registers commands with JDA

## InteractionConfig
Config values we will need for the interaction system include:
- I don't know

## Speed (Indexing)
In the future we could use module indexing to index interactions so that
the InteractionService only needs to check a HashTable for a string entry
instead of searching through modules