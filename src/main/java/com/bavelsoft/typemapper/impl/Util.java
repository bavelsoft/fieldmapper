package com.bavelsoft.typemapper.impl;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import java.util.function.Supplier;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;
import javax.lang.model.util.Types;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

/*
 * Code here is not specific to this project.
 */
class Util {
	static TypeMirror paramType(Element e) {
		ExecutableElement ee = (ExecutableElement)e;
		return ee.getParameters().size() == 1 ? ee.getParameters().get(0).asType() : null;
	}

	static TypeMirror returnType(Element e) {
		ExecutableElement ee = (ExecutableElement)e;
		TypeMirror returnType =  ee.getReturnType();
		return returnType.getKind() == TypeKind.VOID ? null : returnType;
	}

	static <T> T classValue(Supplier<Class<T>> f) {
		String className;
		try {
			Class<T> clazz = f.get();
			className = clazz.getCanonicalName();
		} catch (MirroredTypeException mte) {
			//TODO should this work?
			DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
			TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
			className = classTypeElement.getQualifiedName().toString();
		}
		try {
			return (T)Class.forName(className).newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	static AnnotationMirror getAnnotationMirror(Element element, Class<?> clazz) {
		String clazzName = clazz.getName();
		for(AnnotationMirror m : element.getAnnotationMirrors())
			if(m.getAnnotationType().toString().equals(clazzName))
				return m;
		return null;
	}

	static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
		if (annotationMirror == null)
			return null;
		for(Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet() )
			if(entry.getKey().getSimpleName().toString().equals(key))
				return entry.getValue();
		return null;
	}

	static boolean isAbstract(Element method) {
		Set<Modifier> modifiers = method.getModifiers();
		if (method.getEnclosingElement().getKind() == ElementKind.INTERFACE)
			return !modifiers.contains(Modifier.STATIC) && !modifiers.contains(Modifier.DEFAULT);
		else
			return modifiers.contains(Modifier.ABSTRACT);
	}

	static Element asElement(TypeMirror t) {
		if (t instanceof DeclaredType)
			return ((DeclaredType)t).asElement();
		else
			return null;
	}
}
