package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VersionMonitorTest{
    //Basic VersionMonitor object
    private VersionMonitor check;

    public VersionMonitorTest() {

    }

    @Before
    public void setUp() throws Exception {
        check = new VersionMonitor();
    }

    /**
     * @throws java.lang.Exception
     */

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link bgu.spl.a2.VersionMonitor#getVersion()}
     * Checks if an exception is thrown .
     */
    @Test
    public void testGetVersion() {
        int version = 0;
        assertEquals(version,check.getVersion());
    }

    /**
     * Test method for {@link bgu.spl.a2.VersionMonitor#getVersion()}
     * Checks if an exception is thrown .
     */
    @Test
    public void testInc() {
        int version = 0;
        check.inc();
        assertEquals(version+1,check.getVersion());
    }


    /**
     * Test method for {@link bgu.spl.a2.VersionMonitor#await(int)} ()}
     * Checks if an exception is thrown .
     */
    @Test
    public void testAwait() {
        Thread trial=new Thread(() -> {
            while(true) {
                check.inc();
            }
        });
        trial.start();
        int init = check.getVersion();
        try {
            check.await(init);
        } catch (java.lang.InterruptedException e ) {
            //Nothing to do in this case
        }
        assertNotSame(init,check.getVersion());
        trial.interrupt();



    }

}
