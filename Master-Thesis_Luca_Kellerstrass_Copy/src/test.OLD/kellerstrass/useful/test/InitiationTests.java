package kellerstrass.useful.test;

import static org.junit.Assert.*;
import org.junit.Test;

import kellerstrass.useful.*;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;


/**
 * This test class tests the methods, implemented in kellerstrass.useful, <br>
 * that are build for initiation checks
 * @author lucak
 *
 */
public class InitiationTests {

	@Test
	/**
	 * This test tests the initiation test, if the array has negative values in it
	 */
	public  void hasOnlyPositiveValuesTest() {
        double[] array1 = {1.0, -1.0, 2.0, 3.0, 0.0};
        double[] array2 = {1.0, 0.0001, 2.0, 3.0, 0.0};    
        boolean hasNotNegative= Checking.hasOnlyPositiveValues(array1);
        boolean hasNegativ2 = Checking.hasOnlyPositiveValues(array2);
        assertEquals(hasNotNegative, false); 
        assertEquals(hasNegativ2, true);
	}
	
	@Test
	/**
	 * This test tests the initiation test, if the given discount curve array really is one an changes it if necessary
	 */
	public  void checkDiscountCurveTest() throws Exception {
		
		//The Time Discretization
		TimeDiscretization td = new TimeDiscretizationFromArray(0.0, 10, 0.1);
		
		//The discount curve candidates
		double[] array1 ={1.0, 0.9, 0.8, 0.75, 0.7, 0.65, 0.6, 0.55, 0.5 , 0.45, 0.4 };   // 11 entries
		double[] array2 ={0.9, 0.8, 0.75, 0.7, 0.65, 0.6, 0.55, 0.5 , 0.45, 0.4 };   // 10 entries
		double[] array3 ={-1.0, 0.9, 0.8, 0.75, 0.7, 0.65, 0.6, 0.55, 0.5 , 0.45, 0.4 };   // negative value
		double[] array4 ={1.0, 0.9, 0.8, 0.75, 0.7, 0.65, 0.6, 0.55, 0.5};   // too short
		double[] array5 ={1.0, 0.9, 0.8, 0.75, 0.7, 0.65, 0.6, 0.55, 0.5 , 0.45, 0.4 };   // too long
		double[] array6 ={1.0, 0.9, 0.8, 0.75, 0.7, 0.65, 0.6, 0.55, 0.5 , 0.45, 0.4 };   // invalid entries
		
		
        //Check them all
	
			double[] finalArray1 = Checking.checkDiscountCurve(array1, td);
			assertEquals(finalArray1.length, 11); 
			
			double[] finalArray2 = Checking.checkDiscountCurve(array2, td);
			assertEquals(finalArray2.length, 11); 
			
		

	}

}
