package net.sourceforge.openarch.junit;

import net.sourceforge.openarch.junit.Parameterized.NonParameterized;

import org.junit.Test;
import org.junit.runner.RunWith;

/*
 * Make sure that this case works
 */
@RunWith(value = Parameterized.class)
public class TestNoneParameterized extends AbstractTestParameterized {

    public TestNoneParameterized(int param) {
        super(param);
    }

    @NonParameterized
    @Test
    public void testNonParameterized() {
        test();
    }

}
