package com.bavelsoft.typemapper;

import java.util.function.BiFunction;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

public class MatchDefault implements BiFunction<Collection<String>, Collection<String>, Map<String, String>> {
	@Override
	public Map<String, String> apply(Collection<String> dstFields, Collection<String> srcFields) {
		Map<String, String> map = new HashMap<>();
		//this match is as lenient as possible
		for (String dstField : dstFields) {
			String field = normalized(dstField);
			if (dstField.startsWith("set") && Character.isUpperCase(dstField.charAt(3)))
				field = field.substring(3);
			for (String srcField : srcFields) {
				if (normalized(srcField).matches(".*"+field+".*")) {
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
