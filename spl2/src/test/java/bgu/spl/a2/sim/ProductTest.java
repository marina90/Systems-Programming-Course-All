package bgu.spl.a2.sim;

import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by acepace on 25/12/2016.
 */
public class ProductTest {
    String productName = "test";
    int startId = 1337;
    int finalId = 4150;
    Product[] partList = {new Product(0,"Glass"),new Product(1,"touch-controller")};

    Product driver = new Product(startId,productName);
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void getName() throws Exception {
        assertEquals(productName,driver.getName());
    }

    @Test
    public void getStartId() throws Exception {
        assertEquals(startId,driver.getStartId());
    }

    @Test
    public void setFinalId() throws Exception {
        driver.setFinalId(finalId);
        assertEquals(finalId,driver.getFinalId());
    }

    private void addPartsArray() {
        Arrays.stream(partList).forEach((item)->driver.addPart(item));
    }

    @Test
    public void addPart() throws Exception {
        addPartsArray();
        List<Product> recvArray = driver.getParts();
        for (int i = 0; i < partList.length; i++) {
            if ((!recvArray.get(i).getName().equals(partList[i].getName())) ||
                (recvArray.get(i).getStartId() != partList[i].getStartId()))
            {
                fail("Non matching objects");
            }
        }

    }

}