package kellerstrass.useful;

public class PaymentOffsetCode {
	
	
	
	public static  String getPaymentOffsetCodeForDouble(double number) throws Exception {
		
		
		String PaymentOffsetCode;
		
		if(number == (1.0/12.0)) {
			PaymentOffsetCode = "1M";
		}
		else { if(number == 0.25) {
			PaymentOffsetCode = "3M";
		}else {if(number == 0.5) {
			PaymentOffsetCode = "6M";
		}else {if(number == 1.0) {
			PaymentOffsetCode = "1Y";
		}else {   PaymentOffsetCode = null;  
		throw new Exception("Payment offset number not valid or not implemented");			
		}			
		}			
		}			
		}		
		return PaymentOffsetCode;	
	}
	
public static  double getPaymentOffSetFromString(String paymentOffsetCode) throws Exception {
	
		switch(paymentOffsetCode) {
		case "1M":
			return (1.0/12.0);
		case "3M":
			return 0.25;
		case "6M":
			return 0.5;
		case "1Y":
			return 1.0;
		default:
			throw new Exception("Payment offset code not valid or not implemented");			
		}
	
	}




	
	

}
