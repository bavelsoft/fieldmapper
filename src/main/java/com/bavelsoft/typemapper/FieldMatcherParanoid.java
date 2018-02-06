package com.bavelsoft.typemapper;

import java.util.function.BiFunction;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import com.bavelsoft.typemapper.FieldMatcher.StringPair;

public class FieldMatcherParanoid implements FieldMatcher {
	@Override
	public void match(Map<String, StringPair> matches, Collection<String> dstFields, Collection<StringPair> srcFields) {
		Map<String, StringPair> explicitMatches = new HashMap<>(matches);
		Iterator<String> dstFieldsIt = dstFields.iterator();
		while (dstFieldsIt.hasNext()) {
			String dstField = dstFieldsIt.next();
			if (explicitMatches.containsKey(dstField)) {
				dstFieldsIt.remove();
				continue;
			}
			String field = normalized(dstField);
			//TODO don't chop if there's another field that starts with the string
			if (dstField.startsWith("set") && Character.isUpperCase(dstField.charAt(3)))
				field = field.substring(3);
			Iterator<StringPair> srcFieldsIt = srcFields.iterator();
			while (srcFieldsIt.hasNext()) {
				StringPair srcField = srcFieldsIt.next();
				String srcFieldName;
				if (srcField.fieldName().startsWith("get") && Character.isUpperCase(dstField.charAt(3)))
					srcFieldName = srcField.fieldName().substring(3);
				else if (srcField.fieldName().startsWith("is") && Character.isUpperCase(dstField.charAt(2)))
					srcFieldName = srcField.fieldName().substring(2);
				else
					srcFieldName = srcField.fieldName();
				if (normalized(srcFieldName).equals(field)) {
					if (!explicitMatches.containsKey(dstField)) {
						if (matches.containsKey(dstField)) {
							throwIfAmbiguous(dstField);
						} else {
							matches.put(dstField, srcField);
							srcFieldsIt.remove();
							dstFieldsIt.remove();
						}
					}
				}
			}
		}
		throwIfDstFieldsUnmatched(dstFields);
		throwIfSrcFieldsUnmatched(srcFields);
	}

	protected void throwIfAmbiguous(String dstField) {
		throw new ExpectedException("Ambiguous mapping for "+dstField);
	}

	protected void throwIfDstFieldsUnmatched(Collection<String> unmatchedDstFields) {
		if (!unmatchedDstFields.isEmpty())
			throw new ExpectedException("Dst fields not matched: "+unmatchedDstFields);
	}

	protected void throwIfSrcFieldsUnmatched(Collection<StringPair> unmatchedSrcFields) {
		if (!unmatchedSrcFields.isEmpty())
			throw new ExpectedException("Src fields not matched: "+unmatchedSrcFields);
	}

	//TODO automated tests for exceptions

	private static String normalized(String s) {
		return s.toLowerCase().replaceAll("_", "");
	}
}
