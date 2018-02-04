package com.bavelsoft.typemapper;

import java.util.function.BiFunction;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import com.google.auto.value.AutoValue;

public interface FieldMatcher {
	Map<String, StringPair> match(Collection<String> dstFields, Collection<StringPair> srcFields);

	@AutoValue
	abstract class StringPair {
		public static StringPair create(String paramName, String fieldName) {
			return new AutoValue_FieldMatcher_StringPair(paramName, fieldName);
		}
		public abstract String paramName();
		public abstract String fieldName();
		public String toString() { return paramName()+"."+fieldName(); }
	}
}
