package com.bavelsoft.typemapper.matcher;

import java.util.Collection;
import com.bavelsoft.typemapper.FieldMatcher.StringPair;
import com.bavelsoft.typemapper.ExpectedException;

public class FieldMatcherDefault extends FieldMatcherParanoid {
	@Override
	protected void throwIfSourceFieldsUnmatched(Collection<StringPair> unmatchedSourceFields) {
		//don't throw
	}
}
