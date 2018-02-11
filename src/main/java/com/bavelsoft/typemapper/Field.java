package com.bavelsoft.typemapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import java.util.function.Function;

@Target(value={ElementType.METHOD})
@Repeatable(Fields.class)
public @interface Field {
	String source();
	String target();
	//Class mapper() default MapperDefault.class;
}

