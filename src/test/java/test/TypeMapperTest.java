package test;

import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TypeMapperTest {
	public interface Foo {
		@TypeMap
		MyDst f(MySrc src);

		default int doo(int i) { return i*2; }
	}

	@Test public void simple()  {
		Foo mapper = new TypeMapperTest_FooTypeMapper();
		MyDst dst = mapper.f(new MySrc(123));
		assertEquals(246, dst.x);
	}
}
