/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.test;

import bgu.spl.a2.Task;
import bgu.spl.a2.WorkStealingThreadPool;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class MergeSort extends Task<int[]> {

    public final int[] arr;

    public MergeSort(int[] array) {
        this.arr = array;
    }

    private int[] leftHalf(int[] array_to_partition) {
        int endIndex = array_to_partition.length / 2;
        int[] left = Arrays.copyOfRange(array_to_partition,0,endIndex);
        return left;
    }
    private int[] rightHalf(int[] array_to_partition) {
        int startIndex = array_to_partition.length / 2;
        int[] right = Arrays.copyOfRange(array_to_partition,startIndex,array_to_partition.length);
        return right;
    }

    private int[] Merge( int[] left, int[] right) {

        int i = left.length - 1;
        int j = right.length - 1;
        int index_in_result = arr.length;

        while (index_in_result > 0) {
            if (j < 0 || (i >= 0 && left[i] >= right[j])) {
                arr[--index_in_result] = left[i--];
            } else {
                arr[--index_in_result] = right[j--];
            }
        }
        return arr;
    }

    @Override
    protected void start() {
        if (arr.length > 1) {
            MergeSort left = new MergeSort(leftHalf(arr));
            MergeSort right = new MergeSort(rightHalf(arr));

            Runnable whenResolvedCallback =  ()->{
                int[] result= Merge(left.getResult().get(),right.getResult().get());
                complete(result);
            };
            MergeSort[] waitFor = new MergeSort[2];
            waitFor[0]= right;
            waitFor[1]= left;
            spawn(right,left);

            whenResolved(Arrays.asList(waitFor),whenResolvedCallback);
        }
        else{
            complete(arr);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        WorkStealingThreadPool pool = new WorkStealingThreadPool(7);
        int n = 500000; //you may check on different number of elements if you like
        int[] array = new Random().ints(n).toArray();

        MergeSort task = new MergeSort(array);

        CountDownLatch l = new CountDownLatch(1);
        pool.start();
        pool.submit(task);
        task.getResult().whenResolved(() -> {
            //warning - a large print!! - you can remove this line if you wish
            //System.out.println(Arrays.toString(task.getResult().get()));
            System.out.println("Victory");
            l.countDown();
        });

        l.await();
        pool.shutdown();

    }

}
