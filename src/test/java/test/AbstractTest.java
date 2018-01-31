package test;

import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class AbstractTest {
	public interface Moo {
		@TypeMap
		MyDst f(MySrc src);

		void g();
	}

	@Test public void simple()  {
		Moo mapper = new AbstractTest_MooTypeMapper() { public void g() {} };
		MyDst dst = mapper.f(new MySrc(123));
		assertEquals(123, dst.x);
	}
}
