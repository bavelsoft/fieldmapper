package com.bavelsoft.typemapper;

import java.util.function.BiFunction;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

public class FieldMatcherDefault implements FieldMatcher {
	@Override
	public void match(Map<String, StringPair> matches, Collection<String> dstFields, Collection<FieldMatcher.StringPair> srcFields) {
		//this match is as lenient as possible
		for (String dstField : dstFields) {
			String field = normalized(dstField);
//TODO don't chop if there's another field that starts with the string
			if (dstField.startsWith("set") && Character.isUpperCase(dstField.charAt(3)))
				field = field.substring(3);
//TODO check for ambiguity
			for (StringPair srcField : srcFields) {
				String srcFieldName;
				if (srcField.fieldName().startsWith("get") && Character.isUpperCase(dstField.charAt(3)))
					srcFieldName = srcField.fieldName().substring(3);
				else if (srcField.fieldName().startsWith("is") && Character.isUpperCase(dstField.charAt(2)))
					srcFieldName = srcField.fieldName().substring(2);
				else
					srcFieldName = srcField.fieldName();
				if (normalized(srcFieldName).equals(field)) {
					matches.put(dstField, srcField);
				}
			}
		}
	}

	private static String normalized(String s) {
		return s.toLowerCase().replaceAll("_", "");
	}
}
