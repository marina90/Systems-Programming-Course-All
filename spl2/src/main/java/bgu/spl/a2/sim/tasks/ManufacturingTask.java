package bgu.spl.a2.sim.tasks;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.Task;
import bgu.spl.a2.sim.Product;
import bgu.spl.a2.sim.Warehouse;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tools.Tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Created by Marina.Izmailov on 12/28/2016.
 */
public class ManufacturingTask extends Task<Product> {
    private Product product;
    private Warehouse storage;
    private ManufactoringPlan build;

    /**
     * Constructor
     * @param product -  The Product we are using
     * @param storage    - The Warehouse the Product uses
     */
    public ManufacturingTask(Product product, Warehouse storage) {
        this.product = product;
        this.storage = storage;
        this.build = storage.getPlan(product.getName());
    }

    @Override
    protected void start() {

        Product [] myParts = new Product[build.getParts().length];
        ManufacturingTask [] partRun = new ManufacturingTask[myParts.length];
        for(int i = 0 ;i < build.getParts().length; i++){
                myParts[i] = new Product(product.getStartId()+1,build.getParts()[i]);
                product.addPart(myParts[i]);
                partRun[i] = new ManufacturingTask(myParts[i],storage);
        }
        spawn(partRun);

        if (partRun.length ==0 ) { //Because the Staff Part1 implementation does not handle 0 length task lists.
            this.partsDone();
        } else {
            whenResolved(Arrays.asList(partRun), this::partsDone);
        }
    }

    /**
     * Callback that occures once all the parts have been processed.
     * Responsible for acquiring tools and using them
     */
    private void partsDone() {
        int toolLength = build.getTools().length;
        if (0 == toolLength) { //edge case of a product with no tool list
            doneTask();
            return;
        }

        ArrayList<Deferred<Tool>> myTools = new ArrayList<>(build.getTools().length);
        Stream.of(build.getTools()).forEach(n->myTools.add(storage.acquireTool(n)));

        AtomicInteger toolCounter = new AtomicInteger(myTools.size());
        for (Deferred<Tool> tool:myTools) {
            tool.whenResolved(()->{
                product.addToFinalId(tool.get().useOn(product));
                ToolRelease toRelease=  new ToolRelease(tool.get(),storage);
                spawn(toRelease);
                if (0 == toolCounter.decrementAndGet()) {
                    doneTask();
                }
            });
        }
    }

    /**
     * Calculates final ID and completes the task
     */
    private void doneTask() {
        product.addToFinalId(product.getStartId());
        complete(product);
    }
}
