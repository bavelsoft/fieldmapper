package test;

import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class AbstractTest {
	public interface Moo {
		@TypeMap
		MyTarget f(MySource source);

		void g();
	}

	public static abstract class Woo {
		@TypeMap
		abstract MyTarget f(MySource source);

		abstract void g();
	}

	@Test public void testInterface()  {
		Moo mapper = new AbstractTest_MooTypeMapper() { public void g() {} };
		MyTarget target = mapper.f(new MySource(123));
		assertEquals(123, target.x);
	}

	@Test public void testAbstractClass()  {
		Woo mapper = new AbstractTest_WooTypeMapper() { public void g() {} };
		MyTarget target = mapper.f(new MySource(123));
		assertEquals(123, target.x);
	}
}
