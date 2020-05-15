package kellerstrass.swap.test;

import kellerstrass.swap.StoredSwap;
import net.finmath.montecarlo.interestrate.products.Swap;

public class StoredSwapTest {

	public static void main(String[] args) {


		//Initialization of an example Swap
		
		StoredSwap testStoredSwap = new StoredSwap("Example");
		Swap testSwap = testStoredSwap.getSwap();
		System.out.println("Initialization of the swap worked");
		
		System.out.println("The name of the swap is " + testStoredSwap.getSwapName());

	}

}
