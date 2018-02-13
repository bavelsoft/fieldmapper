package com.bavelsoft.typemapper.match;

import java.util.Collection;
import com.bavelsoft.typemapper.ExpectedException;

public class FieldMatchSource extends FieldMatchParanoid {
	@Override
	protected void throwIfTargetFieldsUnmatched(Collection<String> unmatchedTargetFields) {
		//don't throw
	}
}
