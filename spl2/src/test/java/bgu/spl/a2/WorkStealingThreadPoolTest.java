package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by acepace on 27/12/2016.
 */
public class WorkStealingThreadPoolTest {
        class MergeSortTest extends Task<int[]> {
            private final int[] arr;


            public MergeSortTest(int[] array) {
                this.arr = array;
            }

            private int[] leftHalf(int[] array_to_partition) {
                int size1 = array_to_partition.length / 2;
                int[] left = Arrays.copyOfRange(array_to_partition, 0, size1);
                return left;
            }

            private int[] rightHalf(int[] array_to_partition) {
                int size1 = array_to_partition.length / 2;
                int[] right = Arrays.copyOfRange(array_to_partition, size1, array_to_partition.length);
                return right;
            }

            private int[] Merge(int[] left, int[] right) {
                int i1 = 0;
                int i2 = 0;

                for (int i = 0; i < arr.length; i++) {
                    if (i2 >= right.length || (i1 < left.length && left[i1] <= right[i2])) {
                        arr[i] = left[i1];
                        i1++;
                    } else {
                        arr[i] = right[i2];
                        i2++;
                    }
                }
                return arr;
            }

            @Override
            protected void start() {
                if (arr.length > 1) {
                    int[] leftArray = leftHalf(arr);
                    int[] rightArray = rightHalf(arr);

                    MergeSortTest left = new MergeSortTest(leftArray);
                    MergeSortTest right = new MergeSortTest(rightArray);

                    Runnable whenResolvedCallback = () -> {
                        int[] result = Merge(left.getResult().get(), right.getResult().get());
                        complete(result);
                    };
                    List<MergeSortTest> waitFor = new LinkedList<>();
                    waitFor.add(right);
                    waitFor.add(left);
                    spawn(right, left);

                    whenResolved(waitFor, whenResolvedCallback);
                } else {
                    complete(arr);
                }
            }


        }

        class WhenResolveTest extends Task<Void>
        {
            int var_to_update = 0;
            CountDownLatch x = new CountDownLatch(1);
            @Override
            protected void start() {
                Runnable updater = () -> {
                    x.countDown();
                    var_to_update++;
                };

                whenResolved(new ArrayList<>(0), updater);
            }
        }

        public WorkStealingThreadPoolTest() {}

        @Test
        public void taskRunWhenResolvedEmpty() throws InterruptedException {
            WorkStealingThreadPool pool = new WorkStealingThreadPool(4);
            WhenResolveTest x = new WhenResolveTest();
            pool.start();
            pool.submit(x);
            x.x.await();
            pool.shutdown();

            assertEquals(1,x.var_to_update);
        }

        @Test
        public void runMergeTest() throws InterruptedException{
            runThreads(1);
            runThreads(2);
            runThreads(7);
        }

        @Test
        public void shutdown() throws InterruptedException{
            WorkStealingThreadPool pool = new WorkStealingThreadPool(4);
            int n = 90000; //you may check on different number of elements if you like
            int[] array = new Random().ints(n).toArray();

            MergeSortTest task = new MergeSortTest(array);

            CountDownLatch l = new CountDownLatch(1);
            pool.start();
            pool.submit(task);
            pool.shutdown();
            //Check using debugger that all threads died
        }

        private void runThreads(int nThreads) throws InterruptedException{
            WorkStealingThreadPool pool = new WorkStealingThreadPool(nThreads);
            int n = 90000; //you may check on different number of elements if you like
            int[] array = new Random().ints(n).toArray();

            MergeSortTest task = new MergeSortTest(array);

            CountDownLatch l = new CountDownLatch(1);
            pool.start();
            pool.submit(task);
            task.getResult().whenResolved(() -> {
                //warning - a large print!! - you can remove this line if you wish
                //System.out.println(Arrays.toString(task.getResult().get()));
                System.out.println("victory");
                l.countDown();
            });

            l.await();
            pool.shutdown();
        }
}
