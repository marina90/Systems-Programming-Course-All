package bgu.spl.a2.sim;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a product produced during the simulation.
 */
public class Product  implements java.io.Serializable{

	private final long startId;
    private long finalId;
    private final String name;
    private List<Product> productList = new ArrayList<>();

	/**
	 * Constructor
     * @param startId - Product start id
     * @param name    - Product name
     */
	public Product(long startId, String name) {

        this.startId = startId;
        this.name = name;
    }

	/**
	 * @return The product name as a string
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The product start ID as a long. start ID should never be changed.
	 */
	public long getStartId() {
		return startId;
	}

	/**
	 * @return The product final ID as a long.
	 * final ID is the ID the product received as the sum of all UseOn();
	 */
	public long getFinalId() {
		return finalId;
	}
	public void addToFinalId(long sum){this.finalId+=sum;}
	public void setFinalId(long finalId) {this.finalId = finalId;}

	/**
	 * @return Returns all parts of this product as a List of Products
	 */
	public List<Product> getParts() {
	    return productList;
	}

	/**
	 * Add a new part to the product
	 *
	 * @param p - part to be added as a Product object
	 */
	public void addPart(Product p) {
	    productList.add(p);
	}

	@Override
	public String toString(){
		String output = "ProductName: " + getName()+ " Product Id = " +getFinalId();
		output = output.concat(" -- PartsList {");
		for(Product part : getParts()){
			output = output.concat("\n\t"+part.toString());
		}
		output = output.concat(("}"));
		return output;
	}

}
