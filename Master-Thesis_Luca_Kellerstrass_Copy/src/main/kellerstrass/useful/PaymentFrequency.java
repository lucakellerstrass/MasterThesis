package kellerstrass.useful;

public enum PaymentFrequency {

	MOUNTHLY (12),
	QUATERLY (4),
	BIANNUAL (2),
	ANNUAL   (1);
	
	PaymentFrequency(int PaymentsPerYear){
		this.PaymentsPerYear = PaymentsPerYear;
	}
	
	private final int PaymentsPerYear;
	
	public int PaymentsPerYear() {
		return PaymentsPerYear;
	}
	
	
}
