package kellerstrass.useful.test;
import static org.junit.Assert.*;
import org.junit.Test;

import kellerstrass.usefulOLD.PaymentFrequencyOLD;


/**
 * This test class tests the methods, implemented in kellerstrass.useful, <br>
 * that are build for enums
 * @author lucak
 *
 */
public class EnumTests {

	@Test
	/**
	 * This test tests the PaymentFrequency enum 
	 */
	public  void hasOnlyPositiveValuesTest() {
        
		int PaymentsPerYear12 = PaymentFrequencyOLD.MOUNTHLY.PaymentsPerYear();
		int PaymentsPerYear4 = PaymentFrequencyOLD.QUATERLY.PaymentsPerYear();
		int PaymentsPerYear2 = PaymentFrequencyOLD.BIANNUAL.PaymentsPerYear();
		int PaymentsPerYear1 = PaymentFrequencyOLD.ANNUAL.PaymentsPerYear();
		
		 assertEquals(PaymentsPerYear12, 12); 
		 assertEquals(PaymentsPerYear4, 4); 
		 assertEquals(PaymentsPerYear2, 2); 
		 assertEquals(PaymentsPerYear1, 1); 
      
	}

}
