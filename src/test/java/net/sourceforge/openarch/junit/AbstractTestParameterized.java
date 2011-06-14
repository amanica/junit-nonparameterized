package net.sourceforge.openarch.junit;

import java.util.Arrays;
import java.util.Collection;

import net.sourceforge.openarch.junit.Parameterized.Parameters;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

@RunWith(value = Parameterized.class)
public abstract class AbstractTestParameterized {

    protected int param = -1;

    @Rule
    public TestName testName = new TestName();

    public AbstractTestParameterized(int param) {
        this.param = param;
    }

    protected void test() {
        System.out.println(getClass().getSimpleName() + "."
            + testName.getMethodName() + " param=" + param);
    }

    /*
     * For running the tests for all parameters
     */
    @Parameters
    public static Collection getLayoutVersions() {
        Object[][] data = new Object[][] { {0}, {1}, {2}};
        return Arrays.asList(data);
    }
}
