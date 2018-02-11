package com.bavelsoft.typemapper.matcher;

import java.util.Collection;
import com.bavelsoft.typemapper.ExpectedException;

public class FieldMatcherSource extends FieldMatcherParanoid {
	@Override
	protected void throwIfTargetFieldsUnmatched(Collection<String> unmatchedTargetFields) {
		//don't throw
	}
}
