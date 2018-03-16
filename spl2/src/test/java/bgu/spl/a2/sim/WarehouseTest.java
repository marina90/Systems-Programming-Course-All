package bgu.spl.a2.sim;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tools.GcdScrewDriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;
import bgu.spl.a2.sim.tools.Tool;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class WarehouseTest {
    private Warehouse driver = new Warehouse();
    private String planName = "6'-screen";
    private String[] toolList = {"np-hammer", "rs-pliers"};
    private String[] partList = {"glass", "touch-controller"};
    private ManufactoringPlan plan = new ManufactoringPlan(planName,partList,toolList);
    private int numTools = 50;

    @Before
    public void setUp() throws Exception {
        driver.addTool(new RandomSumPliers(),numTools);
        driver.addTool(new GcdScrewDriver(),numTools);
        driver.addTool(new NextPrimeHammer(),numTools);
    }

    @Test
    public void acquireTool() throws Exception {
        Deferred<Tool> toolToAcquire = driver.acquireTool("gs-driver");
        assertEquals(true,toolToAcquire.isResolved());
        Tool x = toolToAcquire.get();
        assertEquals(true, x instanceof GcdScrewDriver);
    }

    @Test
    public void acquireToolAndRelease() throws Exception
    {
        Deferred<Tool> toolToAcquire = driver.acquireTool("gs-driver");
        assertEquals(true,toolToAcquire.isResolved());
        driver.releaseTool(toolToAcquire.get());
    }

    @Test
    public void acquireTooManyToolsAndReleaseOne() throws Exception
    {
        List<Deferred<Tool>> tools = new ArrayList<>(numTools);
        for (int i = 0; i < numTools; i++) {
            tools.add(driver.acquireTool("gs-driver"));
        }
        Deferred<Tool> tooLate = driver.acquireTool("gs-driver");
        assertEquals(false,tooLate.isResolved());
        driver.releaseTool(tools.get(0).get());
        assertEquals(true,tooLate.isResolved());
        assertEquals(true, tooLate.get() instanceof GcdScrewDriver);
    }

    @Test
    public void acquireToolFromAddTools() throws Exception
    {
        List<Deferred<Tool>> tools = new ArrayList<>(numTools);
        for (int i = 0; i < numTools; i++) {
            tools.add(driver.acquireTool("gs-driver"));
        }
        Deferred<Tool> tooLate = driver.acquireTool("gs-driver");
        assertEquals(false,tooLate.isResolved());
        driver.addTool(new GcdScrewDriver(),2);
        assertEquals(true,tooLate.isResolved());
        assertEquals(true, tooLate.get() instanceof GcdScrewDriver);
    }

    @Test
    public void planTest() throws Exception {
        driver.addPlan(plan);
        assertEquals(plan,driver.getPlan(planName));
    }

    @Test
    public void addTool() throws Exception {
        int NumToAdd=25;
        driver.addTool(new GcdScrewDriver(),NumToAdd);
        List<Deferred<Tool>> tools = new ArrayList<>(numTools);
        for (int i = 0; i < numTools+NumToAdd; i++) {
            tools.add(driver.acquireTool("gs-driver"));
        }
        assertEquals(true,tools.get(numTools+NumToAdd-1).isResolved());
    }

}
