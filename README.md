Typemapper is a Java annotation processor for the generation of type-safe and performant mappers for Java bean classes, inspired by Mapstruct, with a [Worse Is Better](https://en.wikipedia.org/wiki/Worse_is_better) approach.

Typemapper generates implementations of @TypeMap annotated methods.

To get automatically generated code for mapping objects of class YourSource to objects of class YourTarget, annotate an abstract method:

    interface Foo {
        @TypeMapper
        YourTarget map(YourSource y);
    }

And you'll get a generated class FooTypeMapper which implements Foo, and which matches up the setters of YourTarget with the getters of YourSource, something like:

    class FooTypeMapper implements Foo {
        public YourTarget map(YourSource y) {
            YourTarget target = new YourTarget();
            target.setBar(y.getBar());
            target.setBaz(y.get_baz());
            target.setQux(y.getQux());
            return target;
        }
    }

The generated code is smart enough to convert between types using a constructor of the target type:

    class YourSource {
        Set getX() {...}
    }

    class YourTarget {
        void setX(ArrayList x) {...} //because ArrayList has a constructor which accepts a Collection
    }

You can also define methods to convert between fields types, and the generated code will call them:

    interface Foo {
        @TypeMapper
        YourTarget map(YourSource y);

        default YourSubTarget map(YourSubSource s) {
            ...
        }
    }

It doesn't matter what the conversion methods are called, so long as their types match. The most specific parameter matches first, and then the least specific return type. You can include conversion methods from other files by extending or implementing other interfaces or classes. Typemapper provides an interface `MapperDefault` with simple conversions, e.g. NullPointer safe unboxing, using toString to convert any object to a String:

    interface Foo extends MapperDefault {
        @TypeMapper
        YourTarget map(YourSource y);
    }

You can also annotate methods that accept multiple parameters, so long as they don't have ambiguously mapped fields:

    interface Foo {
        @TypeMapper
        YourTarget map(YourSource y, YourOtherSource z);
    }

To override (or disambiguate) particular fields, use the @Mapping annotation, always specifying the source parameter name as shown here:

    interface Foo {
        @TypeMapper(fieldMappingsByName={
            @Mapping(source="y.getA()", target="setB()")
            @Mapping(source="z.getB()", target="setC()")
        })
        YourTarget map(YourSource y, YourOtherSource z);
    }

In addition to warning about mapping ambiguity, by default TypeMapper warns about unmapped setters on the destination class. To alter this behavior, configure a different matcher (one of FieldMatchParanoid, FieldMatchSource, FieldMatchRelaxed):

    interface Foo {
        @TypeMapper(matcher=FieldMatchDefault.class)
        YourTarget map(YourSource y);
    }


You can also override the code generated for each field, or at the start or end of the method:

    interface Foo {
        @TypeMapper(perFieldCode = "if (${sourceFields} != null) ${target}.${targetField}(${func}(${sourceField}()))")
        YourTarget map(YourSource y);
    }

If you'd like to use dependency injection, define abstract getter methods and wire up the generated class properly.

    interface Foo {
        @TypeMapper
        YourTarget map(YourSource y);

        default YourSubTarget map(YourSubSource s) {
            return getTranslator().translate(s);
        }

        SubTranslator getTranslator();
    }

    @Singleton
    class MyFooTypeMapper extends FooTypeMapper {
        @Inject SubTranslator translator;

        SubTranslator getTranslator() { return translator; }
    }

Check the [unit tests](src/test/java/test/) for more info.
