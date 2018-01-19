package com.bavelsoft.fieldmapper.impl;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import java.util.function.Supplier;

class Util {
	static TypeMirror paramType(Element e) {
		ExecutableElement ee = (ExecutableElement)e;
		return ee.getParameters().size() == 1 ? ee.getParameters().get(0).asType() : null;
	}

	static TypeMirror returnType(Element e) {
		ExecutableElement ee = (ExecutableElement)e;
		return ee.getReturnType();
	}

	static <T> T classValue(Supplier<Class<T>> f) {
		String className;
		try {
			Class<T> clazz = f.get();
			className = clazz.getCanonicalName();
		} catch (MirroredTypeException mte) {
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
}
