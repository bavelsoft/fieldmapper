package com.bavelsoft.typemapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.function.BiFunction;
import java.util.Collection;
import java.util.Map;
import com.bavelsoft.typemapper.match.FieldMatchDefault;

@Target(value={ElementType.METHOD})
public @interface TypeMap {
	static String TARGET_NAME = "targetName",
		TARGET_TYPE = "targetType",
		TARGET_FIELD_NAME = "targetFieldSetterName",
		TARGET_FIELD_TYPE = "targetFieldType",
		SOURCE_FIELD_GETTER = "sourceFieldGetter",
		SOURCE_FIELD_TYPE = "sourceFieldType", //TODO
		SOURCE_NAME = "sourceName", //TODO as part of perSourceParam
		SOURCE_TYPE = "sourceType", //TODO
		FUNC = "func";

	static String firstCode = "${targetType} ${targetName} = new ${targetType}()";
	String first() default firstCode;

	static String perFieldCode = "${targetName}.${targetFieldSetterName}(${func}(${sourceFieldGetter}))";
	String perField() default perFieldCode;

	static String lastCode = "return ${targetName}";
	String last() default lastCode;

	Class<? extends FieldMatchStrategy> matcher() default FieldMatchDefault.class;

	Mapping[] mappingsByName() default {};

	public @interface Mapping {
		String source();
		String target();
		//Class mapper() default MapperDefault.class;
	}
}

