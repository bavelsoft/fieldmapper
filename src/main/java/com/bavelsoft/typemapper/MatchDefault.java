package com.bavelsoft.typemapper;

import java.util.function.BiFunction;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

public class MatchDefault implements BiFunction<Collection<String>, Collection<String>, Map<String, String>> {
	@Override
	public Map<String, String> apply(Collection<String> dstFields, Collection<String> srcFields) {
		//TODO make it super lenient: chomp set from setters and then loosely match. check for ambiguity, unmatched
		Map<String, String> map = new HashMap<>();
		for (String dstField : dstFields) {
			if (!dstField.startsWith("set"))
				continue;
			String field = dstField.substring(3);
			for (String srcField : srcFields) {
				if (srcField.startsWith("get") && srcField.substring(3).equals(field)) {
					map.put(dstField, srcField);
				}
			}
		}
		return map;
	}
}
