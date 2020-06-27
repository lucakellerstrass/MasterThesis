package kellerstrass.temp;

import kellerstrass.ModelCalibration.CalibrationMachineInterface;
import kellerstrass.ModelCalibration.HWCalibrationMachine;
import kellerstrass.ModelCalibration.LmmCalibrationMachine;
import kellerstrass.PythonComunication.CVAandCalibrationTestForPython;
import kellerstrass.exposure.ExposureMachine;
import kellerstrass.marketInformation.CalibrationInformation;
import kellerstrass.marketInformation.CurveModelDataType;
import kellerstrass.marketInformation.DataScope;
import kellerstrass.marketInformation.DataSource;
import kellerstrass.swap.StoredSwap;
import kellerstrass.useful.StringToUseful;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.Swap;
import net.finmath.montecarlo.interestrate.products.TermStructureMonteCarloProduct;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.optimizer.SolverException;
import net.finmath.time.TimeDiscretizationFromArray;

public class SwaoInitTest {

	static StoredSwap inputSwap;
	private static  Swap swap;
	private  TermStructureMonteCarloProduct swapExposureEstimator;
	private static CalibrationInformation calibrationInformation;
	private static TimeDiscretizationFromArray simulationTimeDiscretization;

//	private static  int NumberOfFactorsHW;
	private static  int NumberOfFactorsLmm;
	private  int numberOfPaths;

	private static  CalibrationMachineInterface lmmCalibrationmashine;
	private  CalibrationMachineInterface hwCalibrationmashine;

	private static  double recoveryRate;
	private static  double[] cdsSpreads10y = new double[10];
	// Counterparty information
	
	
	public static void main(String[] args) throws SolverException {
		
		
		//Inputs
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
		String referencedate = "24.10.2019";
		int numberOfPaths = 1000;
		int NumberOfFactorsLMM = 3;
		String dataSourceInput = "Market23_10_2019";
		String dataScopeInput = "Full Surface";
		String curveModelInput =  "OIS6M2310";
		double Range = 40.0;
		
		
		recoveryRate = recoveryRateInput;
		// Fill in the cdsSpread array
		cdsSpreads10y[0] = cdsSpread1y;
		cdsSpreads10y[1] = cdsSpread2y;
		cdsSpreads10y[2] = cdsSpread3y;
		cdsSpreads10y[3] = cdsSpread4y;
		cdsSpreads10y[4] = cdsSpread5y;
		cdsSpreads10y[5] = cdsSpread6y;
		cdsSpreads10y[6] = cdsSpread7y;
		cdsSpreads10y[7] = cdsSpread8y;
		cdsSpreads10y[8] = cdsSpread9y;
		cdsSpreads10y[9] = cdsSpread10y;
		
		
		DataScope dataScope = StringToUseful.dataScopeFromString(dataScopeInput);
		DataSource dataSource = StringToUseful.dataSourceFromString(dataSourceInput);
		calibrationInformation = new CalibrationInformation(dataScope, dataSource);

		CurveModelDataType curveModelDataType = StringToUseful.getCurveModelDataTypeFromString(curveModelInput);

//		this.NumberOfFactorsHW = NumberOfFactorsHW;
		NumberOfFactorsLmm = NumberOfFactorsLMM;
		numberOfPaths = numberOfPaths;

		
		double lastTime = 40.0;
		double dt = 0.25;
		simulationTimeDiscretization = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);

		// Initialization
		lmmCalibrationmashine = new LmmCalibrationMachine(numberOfPaths,
				NumberOfFactorsLMM, calibrationInformation, curveModelDataType);


		inputSwap = new StoredSwap(SwapName, BuySell, notional, fixedRate, referencedate,
				swapStart, swapEnd, fixedFrequency, floatFrequency, RateFrequency/* , discountCurve, forecastCurve */,
				fixedCouponConvention, xiborCouponConvention);
		
		if (fixedRate == -1.0) {
		// do ATM
		inputSwap.changeToATMswap(lmmCalibrationmashine.getForwardCurve(), lmmCalibrationmashine.getCurveModel());
		}
		
		
		swap = inputSwap.getSwap();
		

		
	
		
		
		

	}

}
