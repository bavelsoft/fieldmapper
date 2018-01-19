package com.bavelsoft.fieldmapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.function.BiFunction;
import java.util.Collection;
import java.util.Map;

@Target(value={ElementType.METHOD})
public @interface FieldMap {
	static String DST = "dst", SRC = "src", DST_FIELD = "dstField", SRC_FIELD = "srcField", DST_TYPE = "dstType", FUNC = "func";

	static String firstCode = "${dstType} ${dst} = new ${dstType}()";
	String first() default firstCode;

	static String perFieldCode = "${dst}.${dstField}(${func}(${src}.${srcField}()))";
	String perField() default perFieldCode;

	static String lastCode = "return ${dst}";
	String last() default lastCode;

	Class<? extends BiFunction<Collection<String>, Collection<String>, Map<String, String>>> match() default MatchDefault.class;
}

