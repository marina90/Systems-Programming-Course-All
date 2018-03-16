package bgu.spl.a2.sim;
import bgu.spl.a2.WorkStealingThreadPool;
import bgu.spl.a2.sim.tasks.ManufacturingTask;
import org.json.simple.parser.ParseException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {
	/**
	 * Begin the simulation
	 * Should not be called before attachWorkStealingThreadPool()
	 */
	private static  WorkStealingThreadPool pool;
	private static JsonHandler jsonHandler;
	private static Warehouse storage;

	public static ConcurrentLinkedQueue<Product> start() {
		ConcurrentLinkedQueue<Product> SimulationResult = new ConcurrentLinkedQueue<Product>();
		jsonHandler.setWaves();
		jsonHandler.setPlans();
		jsonHandler.setTools();
		pool.start();
		for (WaveContainer wave:jsonHandler.wavesContainer) {
			CountDownLatch l = new CountDownLatch(wave.numOrdersFinal);
            for (OrderContainer order:wave.orders) {
				IntStream.range(0, order.getQty()).forEach(n->{
					Product tempProduct =order.getProduct();
					SimulationResult.add(tempProduct);
					ManufacturingTask tmp = new ManufacturingTask(tempProduct,storage);
					tmp.getResult().whenResolved(()->{
						tmp.getResult().get();
						l.countDown();});
					pool.submit(tmp);
				});
            }
			try {
				l.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			pool.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return SimulationResult;
		/**
		 * @return The ConcurrentLinkedQueu with the results of each product with finalID.
		 */
	}


	private static void serialize(ConcurrentLinkedQueue<Product> SimulationResult) {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream("result.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(SimulationResult);
			oos.flush();
			oos.close();
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * attach a WorkStealingThreadPool to the Simulator, this WorkStealingThreadPool will be used to run the simulation
	 *
	 * @param myWorkStealingThreadPool - the WorkStealingThreadPool which will be used by the simulator
	 */
	public static void attachWorkStealingThreadPool(WorkStealingThreadPool myWorkStealingThreadPool) {
		pool = myWorkStealingThreadPool;
	}

	public static void main(String [] args){
		if (args.length != 1) {
			System.err.println("No command line argument passed");
			return;
		}
		try {
			jsonHandler = new JsonHandler(args[0]);
			int numThreads = jsonHandler.getThreads();
			WorkStealingThreadPool myWorkStealingThreadPool = new WorkStealingThreadPool(numThreads);
			attachWorkStealingThreadPool(myWorkStealingThreadPool);
			storage = jsonHandler.storage;
			serialize(start());
		} catch (ParseException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
