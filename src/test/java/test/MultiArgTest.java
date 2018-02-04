package test;

import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MultiArgTest {
	public interface Foo {
		@TypeMap
		MyDst f(MySrc src, MySrc2 src2);
	}

	@Test public void test()  {
		Foo mapper = new MultiArgTest_FooTypeMapper();
		MyDst dst = mapper.f(new MySrc(123), new MySrc2('q'));
		assertEquals(123, dst.x);
		assertEquals('q', dst.z);
	}
}
