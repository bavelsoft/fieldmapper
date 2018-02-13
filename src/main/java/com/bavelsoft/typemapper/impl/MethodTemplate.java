package com.bavelsoft.typemapper.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.text.StrSubstitutor;
import com.bavelsoft.typemapper.ExpectedException;
import com.bavelsoft.typemapper.TypeMap;
import com.bavelsoft.typemapper.FieldMatchStrategy.StringPair;

/*
 * This is the only real stateful class in the project.
 */
class MethodTemplate {
	private final Map<String, Element> targetFields;
	private final Map<StringPair, Element> sourceFields;
	private final Map<String, String> templateData;
	private final StrSubstitutor sub;
	private final Set<EnumMapper> enumMappers;
	private final ExecutableElement methodElement;
	private final Elements elementUtils;
	private final Types typeUtils;

	MethodTemplate(ExecutableElement methodElement, Map<String, Element> targetFields, Map<StringPair, Element> sourceFields, Elements elementUtils, Types typeUtils) {
		this.targetFields = targetFields;
		this.sourceFields = sourceFields;
		this.methodElement = methodElement;
		this.enumMappers = new HashSet<>();
		this.elementUtils = elementUtils;
		this.typeUtils = typeUtils;
		this.templateData = new HashMap<>();
		templateData.put(TypeMap.TARGET_NAME, "target");
		templateData.put(TypeMap.TARGET_TYPE, Util.returnType(methodElement).toString());
		this.sub = new StrSubstitutor(templateData);
	}

	void setPerFieldValues(Map.Entry<String, StringPair> entry) {
		setPerFieldValues(entry, null);
	}

//TODO source field support 

//TODO target field support 

	void setPerFieldValues(Map.Entry<String, StringPair> entry, TypeElement classWithMapMethod) {
		templateData.put(TypeMap.TARGET_FIELD_NAME, entry.getKey());
		templateData.put(TypeMap.SOURCE_FIELD_GETTER, entry.getValue().toString());
			TypeMirror targetType, sourceType;
		try {
			targetType = Util.paramType(targetFields.get(entry.getKey()));
		} catch (NullPointerException e) {
			throw new ExpectedException("no setter for "+entry.getKey());
		}
		try {
			sourceType = Util.returnType(sourceFields.get(entry.getValue()));
		} catch (NullPointerException e) {
			throw new ExpectedException("no getter for "+entry.getValue());
		}
		templateData.put(TypeMap.FUNC, getMapMethodName(targetType, sourceType, classWithMapMethod));
	}

	String replace(String text) {
		try {
			return sub.replace(text);
		} catch (Exception e) {
			throw new ExpectedException("couldn't replace: "+text);
		}
	}

	Set<EnumMapper> getEnumMappers() {
		return enumMappers;
	}

	//TODO refactor
	private String getMapMethodName(TypeMirror targetType, TypeMirror sourceType, TypeElement classWithMapMethod) {
		if (classWithMapMethod == null)
			classWithMapMethod = (TypeElement)methodElement.getEnclosingElement();
		if (sourceType == null || targetType == null)
			return "";
		Element matchingMapMethod = null;
		for (Element e : elementUtils.getAllMembers(classWithMapMethod))
			if (e.getKind() == ElementKind.METHOD) {
				TypeMirror paramType = Util.paramType(e);
				TypeMirror returnType = Util.returnType(e);
				if (paramType == null || returnType == null)
					continue;
				if (typeUtils.isAssignable(sourceType, paramType)
				 && typeUtils.isAssignable(returnType, targetType)) {
					if (matchingMapMethod == null) {
						matchingMapMethod = e;
						continue;
					}
					boolean a = typeUtils.isAssignable(paramType, Util.paramType(matchingMapMethod));
					boolean b = typeUtils.isAssignable(Util.paramType(matchingMapMethod), paramType);
					if (a && !b) {
						matchingMapMethod = e;
						continue;
					} else if (b && !a) {
						//leave matchingMapMethod
						continue;
					}
					boolean d = typeUtils.isAssignable(returnType, Util.returnType(matchingMapMethod));
					boolean c = typeUtils.isAssignable(Util.returnType(matchingMapMethod), returnType);
					if (c && !d) {
						matchingMapMethod = e;
						continue;
					} else if (d && !c) {
						//leave matchingMapMethod
						continue;
					}
					throw new ExpectedException("Ambiguous matching methods: "+matchingMapMethod+" and "+e);
				}
			}
		if (matchingMapMethod == null) {
			if (!typeUtils.isSameType(sourceType, targetType)) {
				TypeElement targetElement = (TypeElement)((DeclaredType)targetType).asElement();
				for (Element e : elementUtils.getAllMembers(targetElement))
					if (e.getKind() == ElementKind.CONSTRUCTOR) {
						TypeMirror paramType = Util.paramType(e);
						if (paramType != null && typeUtils.isAssignable(sourceType, paramType))
							return "new "+targetElement.getQualifiedName();
					}
			}
			String enumMapperName = getEnumMapMethodName(sourceType, targetType);
			if (enumMapperName != null) {
				enumMappers.add(new EnumMapper(enumMapperName, sourceType, targetType));
				return enumMapperName;
			}
			return "";
		} else {
			return matchingMapMethod.getSimpleName().toString();
		}
	}

	static class EnumMapper {
		String name;
		TypeMirror sourceType, targetType;

		EnumMapper(String name, TypeMirror sourceType, TypeMirror targetType) {
			this.name = name;
			this.sourceType = sourceType;
			this.targetType = targetType;
		}
	}

	private static String getEnumMapMethodName(TypeMirror sourceType, TypeMirror targetType) {
		if (sourceType instanceof DeclaredType && targetType instanceof DeclaredType
		 && ((DeclaredType)sourceType).asElement().getKind() == ElementKind.ENUM
		 && ((DeclaredType)targetType).asElement().getKind() == ElementKind.ENUM)
			return "to"+((DeclaredType)targetType).asElement().getSimpleName().toString();
		else
			return null;
	}
}
