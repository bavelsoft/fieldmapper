package com.bavelsoft.typemapper.match;

import java.util.Collection;
import com.bavelsoft.typemapper.FieldMatchStrategy.StringPair;
import com.bavelsoft.typemapper.ExpectedException;

public class FieldMatchDefault extends FieldMatchParanoid {
	@Override
	protected void throwIfSourceFieldsUnmatched(Collection<StringPair> unmatchedSourceFields) {
		//don't throw
	}
}
