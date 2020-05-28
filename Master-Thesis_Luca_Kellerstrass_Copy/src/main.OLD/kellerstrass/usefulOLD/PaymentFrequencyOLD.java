package kellerstrass.usefulOLD;

public enum PaymentFrequencyOLD {

	MOUNTHLY (12),
	QUATERLY (4),
	BIANNUAL (2),
	ANNUAL   (1);
	
	PaymentFrequencyOLD(int PaymentsPerYear){
		this.PaymentsPerYear = PaymentsPerYear;
	}
	
	private final int PaymentsPerYear;
	
	public int PaymentsPerYear() {
		return PaymentsPerYear;
	}
	
	
}
