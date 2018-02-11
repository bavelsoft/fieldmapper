package test;

import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MultiArgTest {
	public interface Foo {
		@TypeMap
		MyTarget f(MySource2 source2, MySource3 source3);
	}

	@Test public void test()  {
		Foo mapper = new MultiArgTest_FooTypeMapper();
		MyTarget target = mapper.f(new MySource2('q'), new MySource3(123));
		assertEquals(123, target.x);
		assertEquals('q', target.z);
	}
}
