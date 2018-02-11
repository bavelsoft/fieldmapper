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
import com.bavelsoft.typemapper.ExpectedException;
import com.bavelsoft.typemapper.Field;
import com.bavelsoft.typemapper.Fields;
import com.bavelsoft.typemapper.FieldMatcher;
import com.bavelsoft.typemapper.FieldMatcher.StringPair;

class FieldMatchSupport {
	static final Class<TypeMap> typeMapClass = TypeMap.class;
	static final Class<Field> fieldClass = Field.class;
	static final Class<Fields> fieldsClass = Fields.class;

	static Map<String, StringPair> getMatchedFields(ExecutableElement methodElement, MethodTemplate template) {
		List<String> targetFields = new ArrayList<>(template.getTargetFields());
		List<StringPair> sourceFields = new ArrayList<>(template.getSourceFields());
		Map<String, StringPair> matchedFields = getExplicitFieldMap(methodElement);
		TypeMap annotation = methodElement.getAnnotation(typeMapClass);
		try {
			FieldMatcher matcher = Util.classValue(annotation::matcher);
			matcher.match(matchedFields, targetFields, sourceFields);
			return matchedFields;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new ExpectedException("couldn't match");
		}
	}

	static Map<String, StringPair> getExplicitFieldMap(ExecutableElement methodElement) {
		Collection<AnnotationMirror> mirrors = new ArrayList<>();
		AnnotationMirror a = Util.getAnnotationMirror(methodElement, fieldsClass);
		if (a != null) { 
			for (Object v : (List)Util.getAnnotationValue(a, "value").getValue()) {
				mirrors.add((AnnotationMirror)((AnnotationValue)v).getValue());
			}
		} else {
			a = Util.getAnnotationMirror(methodElement, fieldClass); 
			if (a != null)
				mirrors.add(a);
		}
		Map<String, StringPair> explicitFields = new HashMap<>();
		for (AnnotationMirror m : mirrors) {
			String[] source = Util.getAnnotationValue(m, "source").getValue().toString().split("\\.", 2);

			//TODO error checking
			String target = Util.getAnnotationValue(m, "target").getValue().toString();
			explicitFields.put(chopTrailingParens(target),
				StringPair.create(source[0], source[1]));
		}
		return explicitFields;
	}

	static String chopTrailingParens(String s) {
		if (s.endsWith("()"))
			return s.substring(0, s.length()-2);
		else
			throw new ExpectedException("@Field param `"+s+"' must end with () for now");
	}
}
