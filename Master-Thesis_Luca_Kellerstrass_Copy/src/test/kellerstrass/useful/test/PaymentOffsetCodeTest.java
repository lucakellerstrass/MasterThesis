package kellerstrass.useful.test;

import org.junit.Test;

import kellerstrass.useful.PaymentOffsetCode;

public class PaymentOffsetCodeTest {

	
	@Test
	public  void test() throws Exception {
		
		double a = (1.0/12.0);
		double b = 0.25;
		double c = 0.5;
		double d = 1.0;
		double e= 0.7;
		
		System.out.println("We whant to look if the methode >>getPaymentOffsetCodeForDouble<<   works as it should");
		
		
		System.out.println("PaymentOffsetCode for " + a + " =   " + PaymentOffsetCode.getPaymentOffsetCodeForDouble(a));
		System.out.println("PaymentOffsetCode for " + b + " =   " + PaymentOffsetCode.getPaymentOffsetCodeForDouble(b));
		System.out.println("PaymentOffsetCode for " + c + " =   " + PaymentOffsetCode.getPaymentOffsetCodeForDouble(c));
		System.out.println("PaymentOffsetCode for " + d + " =   " + PaymentOffsetCode.getPaymentOffsetCodeForDouble(d));
		
		try {
			System.out.println("PaymentOffsetCode for " + d + " =   " + PaymentOffsetCode.getPaymentOffsetCodeForDouble(e));
		} catch (Exception e2) {
			System.out.println("Error handling works");
		}

	}

}
