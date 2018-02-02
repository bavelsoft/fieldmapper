package com.bavelsoft.typemapper;

import java.util.function.BiFunction;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

public interface FieldMatcher {
	Map<String, StringPair> match(Collection<String> dstFields, Collection<StringPair> srcFields);

	class StringPair {
		public StringPair(String paramName, String fieldName) {
			this.paramName = paramName;
			this.fieldName = fieldName;
		}
		public final String paramName, fieldName;
	}
}
