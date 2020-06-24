package kellerstrass.PythonComunication.test;

import java.util.ArrayList;
import java.util.Map;

import kellerstrass.PythonComunication.CVAandCalibrationTestForPython;
import net.finmath.optimizer.SolverException;

public class CVAandCalibrationTestForPythonTest {

	public static void main(String[] args) throws SolverException {

		String SwapName = "Swap 1";
		String BuySell  = "Buy";
		int notional = 1000000;
		double fixedRate = 0.00547;
		String swapStart = "1d";
		String swapEnd = "10Y";
		String fixedFrequency = "1Y";
		String floatFrequency = "6M";
		String RateFrequency = "6M";
		/* String discountCurve, String forecastCurve, */
		String fixedCouponConvention = "ACT/365";
		String xiborCouponConvention = "ACT/365";
		String counterpartyName = "counterparty1";
		double recoveryRateInput = 0.4;
		double cdsSpread1y = 6;
		double cdsSpread2y = 9; double cdsSpread3y = 15; double cdsSpread4y= 22; double cdsSpread5y = 35; double cdsSpread6y = 40;
		double cdsSpread7y= 45; double cdsSpread8y = 46; double cdsSpread9y = 46; double cdsSpread10y = 47;

		// Modelling parameters
		String referencedate = "24.10.2019";
		int numberOfPaths = 1000;
		int NumberOfFactorsLMM = 3; /*
																			 * int NumberOfFactorsHW should be
																			 * deleted,
																			 */
		String dataSourceInput = "Market24_10_2019";
		String dataScopeInput = "Full Surface";
		String curveModelInput =  "OIS6M2410";
		double Range = 40.0;
		
		
		
		
		
		
		CVAandCalibrationTestForPython PythonComunication = new CVAandCalibrationTestForPython( SwapName,  BuySell,  notional,  fixedRate,
				 swapStart,  swapEnd,  fixedFrequency,  floatFrequency,  RateFrequency,
				/* String discountCurve, String forecastCurve, */  fixedCouponConvention,
				 xiborCouponConvention,  counterpartyName,  recoveryRateInput,  cdsSpread1y,
				 cdsSpread2y,  cdsSpread3y,  cdsSpread4y,  cdsSpread5y,  cdsSpread6y,
				 cdsSpread7y,  cdsSpread8y,  cdsSpread9y,  cdsSpread10y,

				// Modelling parameters
				 referencedate,  numberOfPaths,  NumberOfFactorsLMM, /*
																					 * int NumberOfFactorsHW should be
																					 * deleted,
																					 */
				 dataSourceInput,  dataScopeInput,  curveModelInput,  Range);
		
		
		ArrayList<Map<String, Object>>  calibrationTable = PythonComunication.printCalibrationTestLmm();
		
		String[] atmExpiries = new String[calibrationTable.size() - 1];
		String[] atmTenors = new String[calibrationTable.size() - 1];
		double[] atmVolatilitiesModel = new double[calibrationTable.size() - 1];
		double[] atmVolatilitiesTarget= new double[calibrationTable.size() - 1];
		String[] atmVolatilitiesDeviation = new String[calibrationTable.size() - 1];

		for (int i = 0; i < calibrationTable.size() - 1; i++) {
			atmExpiries[i] = (String) calibrationTable.get(i).get("Expiry");
			atmTenors[i] = (String) calibrationTable.get(i).get("Tenor");
			atmVolatilitiesModel[i] = (double) calibrationTable.get(i).get("Model_Value");
			atmVolatilitiesTarget[i] = (double) calibrationTable.get(i).get("Target");
			atmVolatilitiesDeviation[i] = (String) calibrationTable.get(i).get("Deviation");
		}
		
		//print it out
		System.out.println("Expiry  \t  Tenor  \t  Model_Value  \t Target  \t   Deviation");
		for(int i = 0; i < calibrationTable.size() - 1; i++) {
			System.out.print(atmExpiries[i]  + "\t");
			System.out.print(atmTenors[i]  + "\t");
			System.out.print(atmVolatilitiesModel[i]  + "\t");
			System.out.print(atmVolatilitiesTarget[i]  + "\t");
			System.out.print(atmVolatilitiesDeviation[i]  + "\n");
			
			
			
		}
		
		
		

	}

}
