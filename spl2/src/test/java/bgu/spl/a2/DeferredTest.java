/**
 *
 */
package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author DG/MI
 *
 */
public class DeferredTest {
    //Basic deferred object
    private Deferred <Integer> check;
    private int var_to_update = 0;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        check=new Deferred<>();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link bgu.spl.a2.Deferred#get()}
     * Checks if get returns the proper value after resolving.
     */
    @Test
    public void testGet() {
        int value = 5;
        check.resolve(value);
        assertEquals(value,(int)check.get());
    }

    /**
     * Test method for {@link bgu.spl.a2.Deferred#get()}
     * Checks get without a resolve, expected exception.
     */
    @Test
    public void testGetException() {
        try {
            check.get();
            fail("SHOULD NOT REACH HERE");
        }
        catch (IllegalStateException e) {

        }
        catch (Exception e) {
            fail("Wrong exception");
        }
    }

    /**
     * Test method for {@link bgu.spl.a2.Deferred#isResolved()}.
     */
    @Test
    public void testIsResolved() {
        assertEquals(false,check.isResolved());
    }

    /**
     * Test method for {@link bgu.spl.a2.Deferred#isResolved()}.
     */
    @Test
    public void testIsResolvedTrue() {
        check.resolve(5);
        assertEquals(true,check.isResolved());
    }

    /**
     * Test method for {@link bgu.spl.a2.Deferred#resolve(java.lang.Object)}
     * checks basic functionality, that resolve works.
     * Equivalent to testGet
     */
    @Test
    public void testResolve() {
        int value = 5;
        check.resolve(value);
        assertEquals(value,(int)check.get());
    }

    /**
     * Test method for {@link bgu.spl.a2.Deferred#resolve(java.lang.Object)} that checks for double invocation.
     */
    @Test
    public void testResolveTwice() {
        check.resolve(5);

        try {
            check.resolve(7);
            fail("SHOULD NOT REACH HERE");
        }
        catch (IllegalStateException e) {
        }
        catch (Exception e) {
            fail("Wrong exception");
        }
    }

    @Test
    public void testResolveRace() {
        List<Callable<Void>> callables = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final int counter = i;
            Callable<Void> updater = () -> {
                check.resolve(counter);
                return null;
            };
            callables.add(updater);
        }

        ExecutorService executor = Executors.newWorkStealingPool(5);
        try {
            executor.invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //now make sure all the threads have run
        //really not the nicest code
        executor.shutdown();
        try {
            while (!executor.isTerminated()) { // the following is similar to
                // Thread.join
                executor.awaitTermination(1, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
        }


    }

    /**
     * Test method for {@link bgu.spl.a2.Deferred#whenResolved(java.lang.Runnable)}.
     * Tests with singlecallback
     */
    @Test
    public void testWhenResolved() {
        Runnable updater = () -> var_to_update++;
        check.whenResolved(updater);
        check.resolve(5);
        assertEquals(1,var_to_update);
    }

    /**
     * Test method for {@link bgu.spl.a2.Deferred#whenResolved(java.lang.Runnable)}.
     * Tests with multiple callbacks
     */
    @Test
    public void testWhenResolvedTwo() {
        Runnable updater = () -> var_to_update++;
        for (int i = 0; i < 5; i++) {
            check.whenResolved(updater);
        }
        check.resolve(5);
        assertEquals(5,var_to_update);
    }

    /**
     * Test method for {@link bgu.spl.a2.Deferred#whenResolved(java.lang.Runnable)}.
     * Tests post resolution
     */
    @Test
    public void testWhenResolvedAlreadyResolved() {
        Runnable updater = () -> var_to_update++;
        check.resolve(5);
        check.whenResolved(updater);
        assertEquals(1,var_to_update);
    }

    /**
     * Test method for {@link bgu.spl.a2.Deferred#whenResolved(java.lang.Runnable)}.
     * Tests registering callbacks from multiple threads at once.
     */
    @Test
    public void testWhenResolvedSimul() {

        Callable<Void> updater = () -> {
            check.whenResolved(() -> var_to_update++);
            return null;
        };

        List<Callable<Void>> callables = Arrays.asList(
                updater,
                updater,
                updater);

        ExecutorService executor = Executors.newWorkStealingPool(5);
        try {
            executor.invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //now make sure all the threads have run
        //really not the nicest code
        executor.shutdown();
        try {
            while (!executor.isTerminated()) { // the following is similar to
                // Thread.join
                executor.awaitTermination(1, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
        }
        assertEquals(0, var_to_update);
        check.resolve(3); //garbage value

        try {
            Thread.sleep(2000); //really crappy hardcoded value
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(3, var_to_update);
    }



}
