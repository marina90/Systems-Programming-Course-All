package bgu.spl.a2.sim.tools;

import bgu.spl.a2.sim.Product;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by acepace on 25/12/2016.
 */
public class NextPrimeHammerTest {
    NextPrimeHammer driver = new NextPrimeHammer();
    private static final String name = "np-hammer";

    @Test
    public void getType() throws Exception {
        assertEquals(driver.getType(),name);
    }

    @Test
    public void useOn() throws Exception {
        Product x = new Product(new Long(50010),"test");
        Product subPart =new Product(new Long(7),"round-stuff");
        subPart.setFinalId(5689);
        x.addPart(subPart);


        assertEquals(5693,driver.useOn(x));
    }

}
