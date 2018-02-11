package test;

import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TypeMapperTest {
	public interface Foo {
		@TypeMap
		MyTarget f(MySource source);

		default int doo(int i) { return i*2; }
	}

	@Test public void test()  {
		Foo mapper = new TypeMapperTest_FooTypeMapper();
		MyTarget target = mapper.f(new MySource(123));
		assertEquals(246, target.x);
	}
}
