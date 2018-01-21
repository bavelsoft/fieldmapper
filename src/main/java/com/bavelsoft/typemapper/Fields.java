package com.bavelsoft.typemapper;
  
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(value={ElementType.METHOD})
public @interface Fields {
	Field[] value() default {};
}
