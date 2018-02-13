package com.bavelsoft.typemapper.impl;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import com.bavelsoft.typemapper.TypeMap;
import com.bavelsoft.typemapper.TypeMap.Mapping;
import com.bavelsoft.typemapper.ExpectedException;
import com.bavelsoft.typemapper.FieldMatchStrategy;
import com.bavelsoft.typemapper.FieldMatchStrategy.StringPair;

class FieldMatcher {
	static final Class<TypeMap> typeMapClass = TypeMap.class;

	static Map<String, StringPair> getMatchedFields(ExecutableElement methodElement, MethodTemplate template) {
		List<String> targetFields = new ArrayList<>(template.getTargetFields());
		List<StringPair> sourceFields = new ArrayList<>(template.getSourceFields());
		TypeMap annotation = methodElement.getAnnotation(typeMapClass);
		Map<String, StringPair> matchedFields = new HashMap<>();
		for (Mapping m : annotation.mappingsByName()) {
			//TODO error checking
			String[] source = m.source().split("\\.", 2);
			matchedFields.put(chopTrailingParens(m.target()),
				StringPair.create(source[0], source[1]));
		}
		try {
			FieldMatchStrategy matcher = Util.classValue(annotation::matcher);
			matcher.match(matchedFields, targetFields, sourceFields);
			return matchedFields;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new ExpectedException("couldn't match");
		}
	}

	private static String chopTrailingParens(String s) {
		if (s.endsWith("()"))
			return s.substring(0, s.length()-2);
		else
			throw new ExpectedException("@Field param `"+s+"' must end with () for now");
	}
}
