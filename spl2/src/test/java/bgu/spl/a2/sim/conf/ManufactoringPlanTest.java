package bgu.spl.a2.sim.conf;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by acepace on 25/12/2016.
 */
public class ManufactoringPlanTest {
    ManufactoringPlan driver;
    String planName = "6'-screen";
    String[] toolList = {"np-hammer", "rs-pliers"};
    String[] partList = {"glass", "touch-controller"};

    @Before
    public void setUp() throws Exception {
        driver = new ManufactoringPlan(planName,partList,toolList);
    }

    @Test
    public void getParts() throws Exception {
        assertEquals(partList,driver.getParts());
    }

    @Test
    public void getProductName() throws Exception {
        assertEquals(planName,driver.getProductName());
    }

    @Test
    public void getTools() throws Exception {
        assertEquals(toolList,driver.getTools());
    }


}