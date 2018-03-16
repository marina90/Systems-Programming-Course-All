package bgu.spl.a2.sim.tools;

import bgu.spl.a2.sim.Product;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by acepace on 25/12/2016.
 */
public class randomSumPliersTest {
    RandomSumPliers driver = new RandomSumPliers();
    private static final String name = "rs-pliers";

    @Test
    public void getType() throws Exception {
        assertEquals(driver.getType(),name);
    }

    @Test
    public void useOn() throws Exception {
        Product x = new Product(new Long(502345602),"test");
        Product y =new Product(new Long(502345603),"round-stuff");
        y.setFinalId(y.getStartId());
        x.addPart(y);
        long value = driver.useOn(x);
        long result = x.getStartId() + value;
        assertEquals(26075821849L,result);
    }

}
