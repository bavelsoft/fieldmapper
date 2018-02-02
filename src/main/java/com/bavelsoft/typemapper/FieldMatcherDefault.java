package com.bavelsoft.typemapper;

import java.util.function.BiFunction;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

public class FieldMatcherDefault implements FieldMatcher {
	@Override
	public Map<String, StringPair> match(Collection<String> dstFields, Collection<FieldMatcher.StringPair> srcFields) {
		Map<String, StringPair> map = new HashMap<>();
		//this match is as lenient as possible
		for (String dstField : dstFields) {
			String field = normalized(dstField);
			if (dstField.startsWith("set") && Character.isUpperCase(dstField.charAt(3)))
				field = field.substring(3);
//TODO check for ambiguity
			for (StringPair srcField : srcFields) {
				if (normalized(srcField.fieldName).matches(".*"+field+".*")) {
					map.put(dstField, srcField);
				}
			}
		}
		return map;
	}

	private static String normalized(String s) {
		return s.toLowerCase().replaceAll("_", "");
	}
}
