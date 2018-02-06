package test;

import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MultiArgTest {
	public interface Foo {
		@TypeMap
		MyDst f(MySrc2 src2, MySrc3 src3);
	}

	@Test public void test()  {
		Foo mapper = new MultiArgTest_FooTypeMapper();
		MyDst dst = mapper.f(new MySrc2('q'), new MySrc3(123));
		assertEquals(123, dst.x);
		assertEquals('q', dst.z);
	}
}
