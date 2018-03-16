package bgu.spl.a2.sim.tools;

import bgu.spl.a2.sim.Product;

import java.math.BigInteger;

/**
 * Created by acepace on 24/12/2016.
 */
public class GcdScrewDriver implements Tool {

    private static final String name = "gs-driver";
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
        long reversedId = reverse(productId);
        BigInteger BI_productId = BigInteger.valueOf(productId);
        BigInteger BI_reversedId = BigInteger.valueOf(reversedId);
        BigInteger gcd = BI_productId.gcd(BI_reversedId );
        return gcd.longValue();
    }

    /**
     * Taken from programming context.
     * Function takes every digit and promotes it to the reversed place.
     * @param x - Long to reverse
     * @return reversed integer
     */
    private long reverse(long x) {
        long rev = 0;
        while (x != 0){
            rev = rev*10 + x%10; //Promote all the prior digits
            x = x/10; //Removes current digit from input
        }
        return rev;
    }
}
