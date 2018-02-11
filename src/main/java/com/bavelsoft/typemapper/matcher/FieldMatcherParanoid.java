package com.bavelsoft.typemapper.matcher;

import java.util.function.BiFunction;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import com.bavelsoft.typemapper.FieldMatcher.StringPair;
import com.bavelsoft.typemapper.FieldMatcher;
import com.bavelsoft.typemapper.ExpectedException;

public class FieldMatcherParanoid implements FieldMatcher {
	@Override
	public void match(Map<String, StringPair> matches, Collection<String> targetFields, Collection<StringPair> sourceFields) {
		Map<String, StringPair> explicitMatches = new HashMap<>(matches);
		Iterator<String> targetFieldsIt = targetFields.iterator();
		while (targetFieldsIt.hasNext()) {
			String targetField = targetFieldsIt.next();
			if (explicitMatches.containsKey(targetField)) {
				targetFieldsIt.remove();
				continue;
			}
			String field = normalized(targetField);
			//TODO don't chop if there's another field that starts with the string
			if (targetField.startsWith("set") && Character.isUpperCase(targetField.charAt(3)))
				field = field.substring(3);
			Iterator<StringPair> sourceFieldsIt = sourceFields.iterator();
			while (sourceFieldsIt.hasNext()) {
				StringPair sourceField = sourceFieldsIt.next();
				String sourceFieldName;
				if (sourceField.fieldName().startsWith("get") && Character.isUpperCase(targetField.charAt(3)))
					sourceFieldName = sourceField.fieldName().substring(3);
				else if (sourceField.fieldName().startsWith("is") && Character.isUpperCase(targetField.charAt(2)))
					sourceFieldName = sourceField.fieldName().substring(2);
				else
					sourceFieldName = sourceField.fieldName();
				if (normalized(sourceFieldName).equals(field)) {
					if (!explicitMatches.containsKey(targetField)) {
						if (matches.containsKey(targetField)) {
							throwIfAmbiguous(targetField);
						} else {
							matches.put(targetField, sourceField);
							sourceFieldsIt.remove();
							targetFieldsIt.remove();
						}
					}
				}
			}
		}
		throwIfTargetFieldsUnmatched(targetFields);
		throwIfSourceFieldsUnmatched(sourceFields);
	}

	protected void throwIfAmbiguous(String targetField) {
		throw new ExpectedException("Ambiguous mapping for "+targetField);
	}

	protected void throwIfTargetFieldsUnmatched(Collection<String> unmatchedTargetFields) {
		if (!unmatchedTargetFields.isEmpty())
			throw new ExpectedException("Target fields not matched: "+unmatchedTargetFields);
	}

	protected void throwIfSourceFieldsUnmatched(Collection<StringPair> unmatchedSourceFields) {
		if (!unmatchedSourceFields.isEmpty())
			throw new ExpectedException("Source fields not matched: "+unmatchedSourceFields);
	}

	//TODO automated tests for exceptions

	private static String normalized(String s) {
		return s.toLowerCase().replaceAll("[_()]", "");
	}
}
