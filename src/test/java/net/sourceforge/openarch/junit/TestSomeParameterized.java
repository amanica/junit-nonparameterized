package net.sourceforge.openarch.junit;

import java.io.IOException;

import net.sourceforge.openarch.junit.Parameterized.NonParameterized;

import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(value = Parameterized.class)
public class TestSomeParameterized extends AbstractTestParameterized {

    public TestSomeParameterized(int param) {
        super(param);
    }

    @NonParameterized
    @Test
    public void testNonParameterized() {
        test();
    }

    @Test
    public void testParameterized() throws IOException {
        test();
    }

}
