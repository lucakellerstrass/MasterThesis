package kellerstrass.PythonComunication;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CVATestForPython {

	private final static NumberFormat formatter2 = new DecimalFormat("0.00",
			new DecimalFormatSymbols(new Locale("en")));
	private final static NumberFormat formatter6 = new DecimalFormat("0.000000",
			new DecimalFormatSymbols(new Locale("en")));
	private static DecimalFormat formatterValue = new DecimalFormat(" ##0.00000;-##0.00000",
			new DecimalFormatSymbols(Locale.ENGLISH));
	
	public static List<Map<String, String>> getCVAOutput(String swapName,
	double notional,
	double fixedRate,
	String swapStart,
	String swapEnd,
	String fixedFrequency,
	String floatFrequancy,
	String RateFrequency,
	String discountCurve,
	String forecastCurve,
	String fixedCouponConvention,
	String xiborCouponConvention,		
	//Counterparty information
	String counterpartyName/*,
	double recoveryRate,
	double[] cdsSpreads, //For years 1-10
	//Modelling parameters
	String today,
	int numberOfPaths,
	int NumberOfFactorsLMM,
	int NumberOfFactorsHW,
	String dataSource,
	String dataScope,
	String curveModel*/) {
	
		//Read the swap parameters

		//IF Buy then multiply notional with minus one
	
		//Now initiate the swap
		
		//Counterparty information

		
		
		//Modelling parameters

		
		
		
		boolean forcedCalculation = false;
		
		
		
		
		
		return null;
		// TODO Auto-generated method stub

	}

}
