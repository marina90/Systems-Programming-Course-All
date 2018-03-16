package bgu.spl.a2;


import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * represents a work stealing thread pool - to understand what this class does
 * please refer to your assignment.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class WorkStealingThreadPool {

    /**
     * Created by Marina.Izmailov on 12/23/2016.
     */
    private class ProcessorContainer {
        int proccessId;
        ConcurrentLinkedDeque<Task<?>> processQueue;
        Processor processor;
        Thread processThread;

        ProcessorContainer(int id,Processor processor){
            this.proccessId = id;
            this.processor = processor;
            this.processQueue = new ConcurrentLinkedDeque<>();
            this.processThread = new Thread(processor);
        }
    }

    private int nthreads;
    private ProcessorContainer[] processorArrayHelper;
    private VersionMonitor version = new VersionMonitor();
    private Random place = new Random();

    /**
     * creates a {@link WorkStealingThreadPool} which has nthreads
     * {@link Processor}s. Note, threads should not get started until calling to
     * the {@link #start()} method.
     *
     * Implementors note: you may not add other constructors to this class nor
     * you allowed to add any other parameter to this constructor - changing
     * this may cause automatic tests to fail..
     *
     * @param nthreads the number of threads that should be started by this
     * thread pool
     */
    public WorkStealingThreadPool(int nthreads) {
        this.nthreads = nthreads;
        processorArrayHelper = new ProcessorContainer[nthreads];
        for (int i =0; i<nthreads ; i++){
            processorArrayHelper[i] = new ProcessorContainer(i,new Processor(i,this));
        }
    }

    /**
     * submits a task to be executed by a processor belongs to this thread pool
     *
     * @param task the task to execute
     */
    public void submit(Task<?> task) {
        int location = place.nextInt(nthreads);
        addTaskToProcessor(task,location);
    }

    /**
     * Adds a task to a specific processors queue and increments the pool's version.
     * @param task -> Task to add
     * @param processorId - Processor to add.
     */
    /* package */ void addTaskToProcessor(Task<?> task, int processorId) {
        processorArrayHelper[processorId].processQueue.add(task);
        version.inc();
    }

    /**
     * closes the thread pool - this method interrupts all the threads and wait
     * for them to stop - it is returns *only* when there are no live threads in
     * the queue.
     *
     * after calling this method - one should not use the queue anymore.
     *
     * @throws InterruptedException if the thread that shut down the threads is
     * interrupted
     * @throws UnsupportedOperationException if the thread that attempts to
     * shutdown the queue is itself a processor of this queue
     */
    public void shutdown() throws InterruptedException,UnsupportedOperationException {
        Optional<ProcessorContainer> box = Arrays.stream(processorArrayHelper)
                .filter(
                    (item)->(item.processThread.getId() == Thread.currentThread().getId())
                ).findFirst();

        if (box.isPresent()) {
            //Pulled myself
            throw new UnsupportedOperationException("Thread that called shutdown is a pool thread");
        }
        for (ProcessorContainer item:processorArrayHelper)
        {
            item.processThread.interrupt();
        }
        for (ProcessorContainer item:processorArrayHelper)
        {
            item.processThread.join();
            item.processQueue.clear();
        }
      }

    /**
     * start the threads belongs to this thread pool
     */
    public void start() {
        Arrays.stream(processorArrayHelper).forEach((item) -> item.processThread.start());
    }

    /**
     * Gets a task for a processor.
     * If the current processors task is empty, attempts to steal
     * @param id - Processor ID
     * @return returns a task, or null if none are available.
     */
    /* package */ Task<?> getTaskForProcessor(int id) {
        if (isEmptyQueue(id)) {
            int tmp = id + 1;
            steal(0, tmp, id);
        }
        return processorArrayHelper[id].processQueue.pollFirst();
    }

    /**
     * 
     * @param counter Number of times the loop is run
     * @param stealId - Id from which we're trying to steal
     * @param id - Who we are, to prevent stealing from myself
     * @return Did we successfully steal a bunch of tasks
     */
    /* package */ private boolean steal(int counter,int stealId,int id) {
        if (counter == nthreads-1) {
            return false;
        }
        if (stealId != id && stealId != nthreads && (!isEmptyQueue(stealId))) {
            int steal = (int)Math.floor(processorArrayHelper[stealId].processQueue.size() / 2);
            while (steal > 0) {
                Task<?> stolenTask = processorArrayHelper[stealId].processQueue.pollLast();
                if (stolenTask == null) {break;}
                processorArrayHelper[id].processQueue.addLast(stolenTask);
                steal--;
            }
            if (!isEmptyQueue(id)) {
                version.inc();
                return true;
            }
        }

        if (stealId == nthreads) {
            return steal(counter+1,0, id);
        } else {
            return steal(counter + 1, stealId + 1, id);
        }
    }

    private boolean isEmptyQueue(int id){
        return (processorArrayHelper[id].processQueue.isEmpty());
    }

    /* package */ VersionMonitor getPoolVersionMonitor() { return version;}

}
