Java object mapping, with a [Worse Is Better](https://en.wikipedia.org/wiki/Worse_is_better) approach.

Typemapper generates implements of interfaces with @TypeMap annotated methods.

For example, for dependency injection, use abstract getter methods and just implement those methods in subclasses of the generated classes.

To get automatically generated code for mapping object of class Y to objects of class X, annotate an abstract method:

    interface Foo {
        @TypeMapper
        X map(Y y);
    }

It matches up the setters of X with the getters of Y. If any of the types don't match up, it will automatically call conversion methods in the class.

    interface Foo {
        @TypeMapper
        X map(Y y);

        default SubX map(SubY s) {
            ...
        }
    }

It doesn't matter what those methods are called, so long as their types match. You can include conversion methods from other files by extending or implementing other interfaces or classes. To get automatic unboxing that doesn't throw NullPointerExceptions, you can extend a builtin interface:

    interface Foo extends MapperDefault {
        @TypeMapper
        X map(Y y);
    }

You can also annotate methods that accept multiple parameters, so long as they don't have ambiguously mapped fields:

    interface Foo {
        @TypeMapper
        X map(Y y, Z z);
    }

To override (or disambiguate) particular fields, use the @Field annotation:

    interface Foo {
        @TypeMapper
	@Field(src="y.getA", dst="setB")
	@Field(src="y.getB", dst="setC")
        X map(Y y, Z z);
    }

You can also override the code generated for each field, or at the start or end of the method:

    interface Foo {
        @TypeMapper(perFieldCode = "if (${srcFields} != null) ${dst}.${dstField}(${func}(${srcField}()))")
        X map(Y y);
    }

TODO elaborate!
