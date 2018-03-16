package bgu.spl.a2;

/**
 * this class represents a single work stealing processor, it is
 * {@link Runnable} so it is suitable to be executed by threads.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 *
 */
public class Processor implements Runnable {

    private final WorkStealingThreadPool pool;
    private final int id;

    /**
     * constructor for this class
     *
     * IMPORTANT:
     * 1) this method is package protected, i.e., only classes inside
     * the same package can access it - you should *not* change it to
     * public/private/protected
     *
     * 2) you may not add other constructors to this class
     * nor you allowed to add any other parameter to this constructor - changing
     * this may cause automatic tests to fail..
     *
     * @param id - the processor id (every processor need to have its own unique
     * id inside its thread pool)
     * @param pool - the thread pool which owns this processor
     */
    /*package*/ Processor(int id, WorkStealingThreadPool pool) {
        this.id = id;
        this.pool = pool;
    }

    /**
     * Adds the given tasks to the current processors queue in the pool.
     * @param task - Task to be added
     */
    /* package */ void addTaskToProcessorQueue(Task<?> task)
    {
        pool.addTaskToProcessor(task,id);
    }

    /**
     * Runs the processor.
     * This function does the following basic algorithm
     * 1 - Get task from pool.
     * 1.1 - If no task exists, wait on the Pool to notify it of any changes that might indicate a task
     * 2 - Run task
     * This loops endlessly untill the hosting thread is Interrupted upon which the function ends gracefully.
     */
    @Override
    public void run() {
        do {
            int version = pool.getPoolVersionMonitor().getVersion();
            Task<?> toRun = pool.getTaskForProcessor(this.id);
            if (toRun != null) {
                toRun.handle(this);
            } else {
                try {
                    waitForChanges(version);
                } catch (InterruptedException e) {
                    break; // We don't have anything to do if interrupted except leave.
                }
            }
            //Do we need to do anything after running the task?
        } while (!Thread.interrupted()); // If thread is interrupted, finish gracefully
    }

    /**
     * Waits on the hosting Pool to change.
     * Returns the moment the pool changes.
     * @throws InterruptedException -> Propagate an InterruptedException if we're interrupted while waiting.
     */
    private void waitForChanges(int oldVersion) throws InterruptedException {
        VersionMonitor vm = pool.getPoolVersionMonitor();
        vm.await(oldVersion);
    }

}
