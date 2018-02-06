package com.bavelsoft.typemapper;

import java.util.Collection;
import com.bavelsoft.typemapper.FieldMatcher.StringPair;

public class FieldMatcherDefault extends FieldMatcherParanoid {
	@Override
	protected void throwIfSrcFieldsUnmatched(Collection<StringPair> unmatchedSrcFields) {
		//don't throw
	}
}
