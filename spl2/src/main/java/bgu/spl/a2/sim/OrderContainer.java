package bgu.spl.a2.sim;

/**
 * Created by Marina.Izmailov on 12/28/2016.
 */
  class OrderContainer {
    private String product;
    private int qty ;
    private long startID ;
    private int counterID =0;

    OrderContainer(String product, int qty, long startID){
        this.product = product;
        this.qty = qty;
        this.startID = startID;

    }

    public Product getProduct() {
        int oldCounter = counterID;
        counterID++;
        return new Product(startID+(oldCounter),product);
    }
    public int getQty() {
        return qty;
    }

    public long getStartID() {
        return startID;
    }

}
