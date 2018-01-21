Java object mapping, with a [Worse Is Better](https://en.wikipedia.org/wiki/Worse_is_better) approach.

Typemapper generates implements of interfaces with @TypeMap annotated methods.

For example, for dependency injection, use abstract getter methods and just implement those methods in subclasses of the generated classes.
