package com.bavelsoft.fieldmapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import java.util.function.Function;

@Target(value={ElementType.METHOD})
@Repeatable(Fields.class)
public @interface Field {
	String src();
	String dst();
	Class mapper() default NullMapper.class;
}

