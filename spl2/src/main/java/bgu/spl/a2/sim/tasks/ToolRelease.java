package bgu.spl.a2.sim.tasks;

import bgu.spl.a2.Task;
import bgu.spl.a2.sim.Warehouse;
import bgu.spl.a2.sim.tools.Tool;

/**
 * Created by marinaiz on 12/30/16.
 */
public class ToolRelease extends Task<Void> {
    private Tool toRelease;
    private Warehouse storage;

    public ToolRelease(Tool toFree, Warehouse storage) {
        toRelease = toFree;
        this.storage = storage;
    }

    @Override
    protected void start() {
        storage.releaseTool(toRelease);
        complete(null);
    }
}
