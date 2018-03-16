package bgu.spl.a2.sim;

import bgu.spl.a2.sim.tools.Tool;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.Deferred;
import bgu.spl.a2.sim.tools.GcdScrewDriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.stream;

/**
 * A class representing the warehouse in your simulation
 * 
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add to this class can
 * only be private!!!
 *
 */
public class Warehouse {

	private Map<String,ManufactoringPlan> plans = new HashMap<>();

	private class ToolContainer {
	    final String toolName;
	    AtomicInteger quantity = new AtomicInteger(0);
        Tool toolSingleton;
        ConcurrentLinkedQueue<Deferred<Tool>> waitingList = new ConcurrentLinkedQueue<>();

        private ToolContainer(String toolName,Tool singleton) {
            this.toolName = toolName;
            this.toolSingleton= singleton;
        }
    }

    private ToolContainer[] tools = {
	        new ToolContainer(GcdScrewDriver.getName(), new GcdScrewDriver()),
	        new ToolContainer(NextPrimeHammer.getName(), new NextPrimeHammer()),
	        new ToolContainer(RandomSumPliers.getName(), new RandomSumPliers())
    };

    /**
     * Returns the appropriate container for the matching tool name!
     * @param name - Tool name, must match
     * @return Either a tool container or null if name didn't exist.
     */
	private ToolContainer getToolContainer(String name) {
        Optional<ToolContainer> box = stream(tools).filter(
                tool -> tool.toolName.equals(name))
                .findFirst();
        return box.orElse(null);
    }

    /**
     * Should never be used outside init!
     * Returns a tool object to be used to pass into addTool, just ugly!
     * @param name - Tool to lookup
     * @return Null or a tool object
     */
    private Tool getToolByName(String name) {
        ToolContainer x = getToolContainer(name);
        if (null == x) {
            return null;
        }
        return x.toolSingleton;
    }

    /**
     * Returns the appropiate container for the tool type
     * @param tool - Tool to search for matching container
     * @return The container :)
     */
    private ToolContainer getToolContainer(Tool tool) {
        return getToolContainer(tool.getType());
    }

	/**
	 * Constructor
	 */
	public Warehouse() {

	}

	/**
	 * Tool acquisition procedure
	 * Note that this procedure is non-blocking and should return immediately
	 *
	 * @param type - string describing the required tool
	 * @return a deferred promise for the  requested tool
	 */
	public Deferred<Tool> acquireTool(String type) {
        Deferred<Tool> toReturn = new Deferred<>();

        ToolContainer container = getToolContainer(type);
        if (null == container) {
            //should never happen
            return toReturn;
        }
        //This is the quantity before we pulled the item
        int currentQty = container.quantity.getAndDecrement();
        if (currentQty <= 0) { //We don't have a tool for us, we create a deferred
            container.quantity.incrementAndGet(); //return our fake tool
            container.waitingList.add(toReturn);
        } else { //yes we do
            toReturn.resolve(createNewTool(container));
        }
        return toReturn;
	}

    /**
     * Releases a specific tool from the waiting list of the toolBox
     * @param toolBox - Toolbox to release
     * @return boolean if we had anything in the list.
     */
    private boolean releaseSpecificTool(ToolContainer toolBox) {
        Deferred<Tool> waiter = tryToResolveWaiter(toolBox);
        if (null == waiter) {
            toolBox.quantity.incrementAndGet();
            return false; //Didn't release anything
        }
        return true; //Released something
    }

    /**
     * Tries to remove a single tool from a toolbox waiting list.
     * If no one is waiting, returns null
     * @param toolBox - Toolbox to withdraw from
     * @return Object that was resolved.
     */
    private Deferred<Tool> tryToResolveWaiter(ToolContainer toolBox) {
        Deferred<Tool> waiter = toolBox.waitingList.poll();
        if (null != waiter) {
            waiter.resolve(createNewTool(toolBox));
        }
        return waiter;
    }

    /**
     * Returns an appropriate tool
     * @param toolBox - The tool type to return
     * @return - A tool object to use
     */
	private Tool createNewTool(ToolContainer toolBox) {
        return toolBox.toolSingleton;
    }

	/**
	 * Tool return procedure - releases a tool which becomes available in the warehouse upon completion.
	 *
	 * @param tool - The tool to be returned
	 */
	public void releaseTool(Tool tool) {
        ToolContainer x = getToolContainer(tool);
        //return if anyone is waiting'
        releaseSpecificTool(x);
    }




    /**
	 * Getter for ManufactoringPlans
	 *
	 * @param product - a string with the product name for which a ManufactoringPlan is desired
	 * @return A ManufactoringPlan for product
	 */
	public ManufactoringPlan getPlan(String product) {
		return plans.get(product);
	}

	/**
	 * Store a ManufactoringPlan in the warehouse for later retrieval
	 *
	 * @param plan - a ManufactoringPlan to be stored
	 */
	public void addPlan(ManufactoringPlan plan) {
		plans.put(plan.getProductName(),plan);
	}



	/**
	 * Store a qty Amount of tools of type tool in the warehouse for later retrieval
	 *
	 * @param tool - type of tool to be stored
	 * @param qty  - amount of tools of type tool to be stored
	 */
	public void addTool(Tool tool, int qty) {
        ToolContainer toolBox = getToolContainer(tool);

        int numLeft = qty;
        List<Deferred<Tool>> toResolve = new ArrayList<>(qty);
        //Pull as many tasks as we can
        for (int i = 0; i < qty; i++) {
            Deferred<Tool> waiter = toolBox.waitingList.poll();
            if (null == waiter) {break;}
            toResolve.add(waiter);
            numLeft--;
        }
        toResolve.forEach((waiter) -> waiter.resolve(createNewTool(toolBox)));
        toolBox.quantity.addAndGet(numLeft);
    }

    void addTool(String toolName,int qty) {
        Tool toAdd = getToolByName(toolName);
        addTool(toAdd,qty);
    }

}
