package bgu.spl.a2.sim.tools;

import bgu.spl.a2.sim.Product;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * Created by acepace on 25/12/2016.
 */
public class gcdScrewDriverTest {
    GcdScrewDriver driver = new GcdScrewDriver();
    private static final String name = "gs-driver";

    @Test
    public void getType() throws Exception {
        assertEquals(driver.getType(),name);
    }

    @Test
    public void useOn() throws Exception {
        int try1 = 7381123;
        BigInteger BI_try1 = java.math.BigInteger.valueOf(try1);
        int try_rev = 3211837;
        BigInteger BI_try2 = java.math.BigInteger.valueOf(try_rev);
        Product x = new Product(new Long(50010),"test");
        Product subPart =new Product(new Long(7381123),"round-stuff");
        subPart.setFinalId(7381123);
        x.addPart(subPart);

        assertEquals(BI_try1.gcd(BI_try2).longValue(),driver.useOn(x));
    }



}
