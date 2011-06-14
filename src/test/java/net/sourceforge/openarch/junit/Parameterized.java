/*
 * This file is a modified version of org.junit.runners.Parameterized
 * which is part of junit and is licensed under the following licence:
 * Common Public License - v 1.0
 * http://www.junit.org/license
 */
package net.sourceforge.openarch.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

/**
 * <p>
 * The custom runner <code>Parameterized</code> implements parameterized tests.
 * When running a parameterized test class, instances are created for the
 * cross-product of the test methods and the test data elements.
 * </p>
 * For example, to test a Fibonacci function, write:
 *
 * <pre>
 * &#064;RunWith(Parameterized.class)
 * public class FibonacciTest {
 *     &#064;Parameters
 *     public static List&lt;Object[]&gt; data() {
 *         return Arrays.asList(new Object[][] {
 *                 Fibonacci,
 *                 { {0, 0}, {1, 1}, {2, 1}, {3, 2}, {4, 3}, {5, 5},
 *                         {6, 8}}});
 *     }
 *
 *     private int fInput;
 *
 *     private int fExpected;
 *
 *     public FibonacciTest(int input, int expected) {
 *         fInput = input;
 *         fExpected = expected;
 *     }
 *
 *     &#064;Test
 *     public void test() {
 *         assertEquals(fExpected, Fibonacci.compute(fInput));
 *     }
 * }
 * </pre>
 * <p>
 * Each instance of <code>FibonacciTest</code> will be constructed using the
 * two-argument constructor and the data values in the
 * <code>&#064;Parameters</code> method.
 * </p>
 */
public class Parameterized extends Suite {

    /**
     * Annotation for a method which provides parameters to be injected into the
     * test class constructor by <code>Parameterized</code>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface Parameters {
    }

    /**
     * Annotation for a methods which should not be parameterized
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface NonParameterized {
    }

    public static class NoTestsException extends Exception {
        private static final long serialVersionUID = 1L;

        public NoTestsException(String message) {
            super(message);
        }
    }

    private class TestClassRunnerForParameters extends
            BlockJUnit4ClassRunner {
        private final int fParameterSetNumber;

        private final List<Object[]> fParameterList;
        int testMethodCount;

        TestClassRunnerForParameters(Class<?> type,
                List<Object[]> parameterList, int i)
                throws InitializationError {
            super(type);
            fParameterList = parameterList;
            fParameterSetNumber = i;
        }

        @Override
        public Object createTest() throws Exception {
            return getTestClass().getOnlyConstructor().newInstance(
                    computeParams());
        }

        private Object[] computeParams() throws Exception {
            try {
                return fParameterList.get(fParameterSetNumber);
            } catch (ClassCastException e) {
                throw new Exception(String.format(
                        "%s.%s() must return a Collection of arrays.",
                        getTestClass().getName(), getParametersMethod(
                                getTestClass()).getName()));
            }
        }

        @Override
        protected String getName() {
            return String.format("[%s]", fParameterSetNumber);
        }

        @Override
        protected String testName(final FrameworkMethod method) {
            return String.format("%s[%s]", method.getName(),
                    fParameterSetNumber);
        }

        @Override
        protected void validateConstructor(List<Throwable> errors) {
            validateOnlyOneConstructor(errors);
        }

        @Override
        protected void collectInitializationErrors(List<Throwable> errors) {
            // super.collectInitializationErrors(errors);
            validatePublicVoidNoArgMethods(BeforeClass.class, true, errors);
            validatePublicVoidNoArgMethods(AfterClass.class, true, errors);

            validateConstructor(errors);

            // validateInstanceMethods(errors);
            validatePublicVoidNoArgMethods(After.class, false, errors);
            validatePublicVoidNoArgMethods(Before.class, false, errors);
            validateTestMethods(errors);

            // validateFields(errors);

            if (computeTestMethods().size() == 0)
                errors.add(new NoTestsException("No runnable methods"));
        }

        @Override
        protected Statement classBlock(RunNotifier notifier) {
            return childrenInvoker(notifier);
        }

        @Override
        protected List<FrameworkMethod> computeTestMethods() {
            List<FrameworkMethod> ret = super.computeTestMethods();
            for (Iterator<FrameworkMethod> i = ret.iterator(); i.hasNext();) {
                FrameworkMethod frameworkMethod =
                    (FrameworkMethod) i.next();
                if (isParameterized() ^
                    !frameworkMethod.getMethod().isAnnotationPresent(
                        NonParameterized.class)) {
                    i.remove();
                }
            }
            testMethodCount = ret.size();
            return ret;
        }

        protected boolean isParameterized() {
            return true;
        }

        @Override
        public int testCount() {
            return testMethodCount;
        }
    }

    private class TestClassRunnerForNonParameterized extends
        TestClassRunnerForParameters {

        TestClassRunnerForNonParameterized(Class<?> type,
            List<Object[]> parameterList, int i)
            throws InitializationError {
            super(type, parameterList, i);
        }

        protected boolean isParameterized() {
            return false;
        }
    }

    private final ArrayList<Runner> runners = new ArrayList<Runner>();

    /**
     * Only called reflectively. Do not use programmatically.
     */
    public Parameterized(Class<?> klass) throws Throwable {
        super(klass, Collections.<Runner> emptyList());
        List<Object[]> parametersList = getParametersList(getTestClass());
        if (parametersList.size() > 0) {
            try {
                runners.add(new TestClassRunnerForNonParameterized(
                        getTestClass().getJavaClass(), parametersList, 0));
            } catch (InitializationError e) {
                for (Throwable t : e.getCauses()) {
                    if (!(t instanceof NoTestsException)) {
                        throw e;
                    }
                }
            }
        }
        try {
            for (int i = 0; i < parametersList.size(); i++) {
                TestClassRunnerForParameters p =
                    new TestClassRunnerForParameters(getTestClass()
                        .getJavaClass(),
                        parametersList, i);
                runners.add(p);
            }
        } catch (InitializationError e) {
            for (Throwable t : e.getCauses()) {
                if (!(t instanceof NoTestsException)) {
                    throw e;
                }
            }
        }
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> getParametersList(TestClass klass)
            throws Throwable {
        return (List<Object[]>) getParametersMethod(klass).invokeExplosively(
                null);
    }

    private FrameworkMethod getParametersMethod(TestClass testClass)
            throws Exception {
        List<FrameworkMethod> methods = testClass
                .getAnnotatedMethods(Parameters.class);
        for (FrameworkMethod each : methods) {
            int modifiers = each.getMethod().getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))
                return each;
        }

        throw new Exception("No public static parameters method on class "
                + testClass.getName());
    }

}
