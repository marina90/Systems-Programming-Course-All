package bgu.spl.a2.sim.tools;

import bgu.spl.a2.sim.Product;

import java.util.Random;
import java.util.stream.LongStream;

/**
 * Created by acepace on 24/12/2016.
 */
public class RandomSumPliers implements Tool {
    private static final long MOD_CONSTANT = 10000;

    private static final String name = "rs-pliers";
    public static String getName() {
        return name;
    }

    @Override
    public String getType() {
        return name;
    }

    @Override
    public long useOn(Product p) {
        long value=0;
        for(Product part : p.getParts()){
            value += Math.abs(useOnSpecificProduct(part));
        }
        return value;
    }

    private long useOnSpecificProduct(Product p) {
        long id = p.getFinalId();
        Random prng = new Random(id);
        long numIntegers = (id % MOD_CONSTANT );
        return LongStream.range(0,numIntegers)
                .map(
                        (x) -> prng.nextInt())
                .sum();
    }
}
