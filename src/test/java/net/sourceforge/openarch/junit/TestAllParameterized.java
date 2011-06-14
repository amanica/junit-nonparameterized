package net.sourceforge.openarch.junit;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

/*
 * Make sure that this case works
 */
@RunWith(value = Parameterized.class)
public class TestAllParameterized extends AbstractTestParameterized {

    public TestAllParameterized(int param) {
        super(param);
    }

    @Test
    public void testParameterized() throws IOException {
        test();
    }

}
