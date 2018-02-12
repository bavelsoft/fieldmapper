Typemapper is a Java annotation processor for the generation of type-safe and performant mappers for Java bean classes, inspired by Mapstruct, with a [Worse Is Better](https://en.wikipedia.org/wiki/Worse_is_better) approach.

Typemapper generates implementations of @TypeMap annotated methods.

To get automatically generated code for mapping objects of class Y to objects of class X, annotate an abstract method:

    interface Foo {
        @TypeMapper
        X map(Y y);
    }

And you'll get a generated class FooTypeMapper which implements Foo, and which matches up the setters of X with the getters of Y, something like:

    class FooTypeMapper implements Foo {
        public X map(Y y) {
            X target = new X();
            target.setA(y.getA());
            target.setWee(y.getWee());
            target.setQux(y.getQux());
            return target;
        }
    }

If any of the types of the getters and setters don't match up, it will automatically call your conversion method:

    interface Foo {
        @TypeMapper
        X map(Y y);

        default SubX map(SubY s) {
            ...
        }
    }

It doesn't matter what the conversion methods are called, so long as their types match. The most specific parameter matches first, and then the least specific return type. You can include conversion methods from other files by extending or implementing other interfaces or classes. To get automatic unboxing that doesn't throw NullPointerExceptions, you can extend or implement a builtin interface:

    interface Foo extends MapperDefault {
        @TypeMapper
        X map(Y y);
    }

You can also annotate methods that accept multiple parameters, so long as they don't have ambiguously mapped fields:

    interface Foo {
        @TypeMapper
        X map(Y y, Z z);
    }

To override (or disambiguate) particular fields, use the @Field annotation, always specifying the source parameter name as shown here:

    interface Foo {
        @TypeMapper
        @Field(source="y.getA", target="setB")
        @Field(source="y.getB", target="setC")
        X map(Y y, Z z);
    }

In addition to warning about mapping ambiguity, by default TypeMapper warns about unmapped setters on the destination class. To alter this behavior, configure a different matcher (one of FieldMatcherParanoid, FieldMatcherSource, FieldMatcherDefault, FieldMatcherRelaxed):

    interface Foo {
        @TypeMapper(matcher=FieldMatcherDefault.class)
        X map(Y y);
    }


You can also override the code generated for each field, or at the start or end of the method:

    interface Foo {
        @TypeMapper(perFieldCode = "if (${sourceFields} != null) ${target}.${targetField}(${func}(${sourceField}()))")
        X map(Y y);
    }

If you'd like to use dependency injection, define abstract getter methods and wire up the generated class properly.

    interface Foo {
        @TypeMapper
        X map(Y y);

        default SubX map(SubY s) {
            return getTranslator().translate(s);
        }

        SubXYTranslator getTranslator();
    }

    @Singleton
    class MyFooTypeMapper extends FooTypeMapper {
        @Inject SubXYTranslator translator;

        SubXYTranslator getTranslator() { return translator; }
    }

Check the (unit tests)[src/test/java/test/] for more info.
