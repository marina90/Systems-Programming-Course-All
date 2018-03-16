package bgu.spl.a2.sim.tools;

import bgu.spl.a2.sim.Product;

import java.math.BigInteger;

/**
 * Created by acepace on 24/12/2016.
 */
public class NextPrimeHammer implements Tool {

    private static final String name = "np-hammer";
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
        long productId = p.getFinalId();
        BigInteger BI_productId = BigInteger.valueOf(productId);
        return BI_productId.nextProbablePrime().longValue();
    }
}
