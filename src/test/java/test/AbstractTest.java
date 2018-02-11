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

	@Test public void test()  {
		Moo mapper = new AbstractTest_MooTypeMapper() { public void g() {} };
		MyTarget target = mapper.f(new MySource(123));
		assertEquals(123, target.x);
	}
}
