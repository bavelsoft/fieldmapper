package test;

import com.bavelsoft.typemapper.TypeMap;
import com.bavelsoft.typemapper.Field;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class FieldTest {
	public interface Foo {
		@TypeMap
		@Field(src="src.getY", dst="setX")
		MyDst f(MySrc src);
	}

	@Test public void test()  {
		Foo mapper = new FieldTest_FooTypeMapper();
		MyDst dst = mapper.f(new MySrc(123, 456));
		assertEquals(456, dst.x);
	}
}
