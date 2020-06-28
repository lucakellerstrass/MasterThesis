package kellerstrass.CVA;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import kellerstrass.ModelCalibration.CalibrationMachineInterface;
import kellerstrass.ModelCalibration.CurveModelCalibrationMachine;
import kellerstrass.ModelCalibration.HWCalibrationMachine;
import kellerstrass.ModelCalibration.LmmCalibrationMachine;
import kellerstrass.PythonComunication.CVAandCalibrationTestForPython;
import kellerstrass.exposure.ExposureMachine;
import kellerstrass.marketInformation.CalibrationInformation;
import kellerstrass.marketInformation.CurveModelDataType;
import kellerstrass.marketInformation.DataScope;
import kellerstrass.marketInformation.DataSource;
import kellerstrass.swap.StoredSwap;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.Swap;
import net.finmath.montecarlo.interestrate.products.TermStructureMonteCarloProduct;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.optimizer.SolverException;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretizationFromArray;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;
import net.finmath.time.daycount.DayCountConvention;

public class CVAComparismUnderAllSwapsExperiment {

	private final static NumberFormat formatter6 = new DecimalFormat("0.000000",
			new DecimalFormatSymbols(new Locale("en")));
	private static DecimalFormat formatterValue = new DecimalFormat(" ##0.00000;-##0.00000",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation = new DecimalFormat(" 0.00000E00;-0.00000E00",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterPercentage = new DecimalFormat(" ##0.000%;-##0.000%",
			new DecimalFormatSymbols(Locale.ENGLISH));

	public static void main(String[] args) throws SolverException, CalculationException  {
		boolean forcedCalculation = false;

		

		// Set the Calibration basket
		DataSource dataSource = DataSource.Market24_10_2019; // relevant for both models

		CalibrationInformation calibrationInformationHW = new CalibrationInformation(DataScope.FullSurface, dataSource);
		CalibrationInformation calibrationInformationFullSurface = new CalibrationInformation(DataScope.FullSurface, dataSource);

		CurveModelCalibrationMachine curveModelCalibrationMaschine = new CurveModelCalibrationMachine(
				CurveModelDataType.OIS6M2410);

		String referenceDate = "24.10.2019";
		int numberOfPaths = 5000;
		int numberOfFactorsM1 = 3; // For Libor Market Model
		int numberOfFactorsM2 = 2; // For Hull white Model

		// Simulation time discretization
		double lastTime = 40.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);

		BrownianMotion brownianMotionHW = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				numberOfFactorsM2, numberOfPaths, 31415 /* seed */);	
		EulerSchemeFromProcessModel processHW = new EulerSchemeFromProcessModel(brownianMotionHW,
				EulerSchemeFromProcessModel.Scheme.EULER);
		CalibrationMachineInterface HWCalibrationMaschine = new HWCalibrationMachine(numberOfPaths, numberOfFactorsM2,
				calibrationInformationHW, curveModelCalibrationMaschine);

		LIBORModelMonteCarloSimulationModel HWsimulationModel = HWCalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(processHW, forcedCalculation);

		
		
		/*
		 * Create an othe rreference HW Model with full vola surface, from where we get the calibrationinfromation of the full surface
		 */	
		EulerSchemeFromProcessModel processRef = new EulerSchemeFromProcessModel(brownianMotionHW,
				EulerSchemeFromProcessModel.Scheme.EULER);
		CalibrationMachineInterface RefCalibrationMaschine = new HWCalibrationMachine(numberOfPaths, numberOfFactorsM2,
				calibrationInformationFullSurface, curveModelCalibrationMaschine);
		
		
		
		
		// We want to use the Hull White Model calibrated swaption volatilities to
				// calibrate the LIBOR Market Model
				double swapPeriodLength = calibrationInformationHW.getSwapPeriodLength();
				String targetVolatilityType = calibrationInformationHW.getTargetVolatilityType();
				LocalDate referenceDate2 = calibrationInformationHW.getReferenceDate();
				BusinessdayCalendarExcludingTARGETHolidays cal = calibrationInformationHW.getCal();
				DayCountConvention modelDC = calibrationInformationHW.getModelDC();
				String DataName = calibrationInformationHW.getName() + " from HullWhite";

				
				ArrayList<Map<String, Object>> calibrationTable = HWCalibrationMaschine.getCalibrationTable(forcedCalculation);
				String[] atmExpiries = new String[calibrationTable.size() - 1];
				String[] atmTenors = new String[calibrationTable.size() - 1];
				double[] atmVolatilitiesByHw = new double[calibrationTable.size() - 1];

				for (int i = 0; i < calibrationTable.size() - 1; i++) {
					atmExpiries[i] = (String) calibrationTable.get(i).get("Expiry");
					atmTenors[i] = (String) calibrationTable.get(i).get("Tenor");
					atmVolatilitiesByHw[i] = (double) calibrationTable.get(i).get("Model_Value");
				}

				CalibrationInformation calibrationInformationLMM = new CalibrationInformation(swapPeriodLength, atmExpiries,
						atmTenors, atmVolatilitiesByHw, targetVolatilityType, referenceDate2, cal, modelDC, DataName);

				
				// brownian motion
				BrownianMotion brownianMotionLMM = new net.finmath.montecarlo.BrownianMotionLazyInit(
						timeDiscretizationFromArray, numberOfFactorsM1, numberOfPaths, 31415 /* seed */);
				// process
				EulerSchemeFromProcessModel processLMM = new EulerSchemeFromProcessModel(brownianMotionLMM,
						EulerSchemeFromProcessModel.Scheme.EULER);
				// calibration machine
				CalibrationMachineInterface LMMCalibrationMaschine = new LmmCalibrationMachine(numberOfPaths, numberOfFactorsM1,
						calibrationInformationLMM, curveModelCalibrationMaschine);
				// simulation machines
				LIBORModelMonteCarloSimulationModel LMsimulationModel = LMMCalibrationMaschine
						.getLIBORModelMonteCarloSimulationModel(processLMM, forcedCalculation);		
		


		
		
		//first test if it would theoretically work
		// Swap
//				StoredSwap testStoredSwap = new StoredSwap("Example 2");				
//				testStoredSwap.changeToATMswap(LMMCalibrationMaschine.getForwardCurve(), LMMCalibrationMaschine.getCurveModel());				
//				Swap testSwap = testStoredSwap.getSwap();
//				
//				double recoveryRate1 = 0.4;
//				double[] cdsSpreads1 =  {6, 9, 15, 22, 35, 40, 45, 45.67, 46.33, 47 };   //{ 300.0, 350.0, 400.0, 450.0, 500.0, 550.0, 600.0, 650.0, 700.0, 750.0 };
//
//				CVA cvaM1 = new CVA(LMsimulationModel, testSwap, recoveryRate1, cdsSpreads1, LMMCalibrationMaschine.getDiscountCurve());
//				CVA cvaM2 = new CVA(HWsimulationModel, testSwap, recoveryRate1, cdsSpreads1, HWCalibrationMaschine.getDiscountCurve());
//				double cvaValueM1 = cvaM1.getValue();
//				double cvaValueM2 = cvaM2.getValue();
//				
//				System.out.println(cvaValueM1 + "   \t   "+cvaValueM2 );
//		
//		
//
//				
//				
				
				
				
				
				
				
				
				
				
		// Swap
		// initiate the swaps that are capture by the volatility matrix for calibration.

		// Basics for the swap
		String BuySell = "Buy";
		int notional = 1000000;
		double fixedRate = -1.0; // We use them ATM // For a rate take 0.00547
		String fixedFrequency = "1Y";
		String floatFrequency = "6M";
		String RateFrequency = "6M";
		String fixedCouponConvention = "ACT/365";
		String xiborCouponConvention = "ACT/365";

		// Counter party information
		double recoveryRate = 0.4;
		double[] cdsSpreads = { 6, 9, 15, 22, 35, 40, 45, 45.67, 46.33, 47 }; // { 300.0, 350.0, 400.0, 450.0, 500.0,
																				// 550.0, 600.0, 650.0, 700.0, 750.0 };

		String[] experiesFromLMM = LMMCalibrationMaschine.getCalibrationItemExpiries(calibrationInformationLMM);
		String[] tenorsFromLMM = LMMCalibrationMaschine.getCalibrationItemTenors(calibrationInformationLMM);

		
		double[] CVADifferenceRelativeToLMM = new double[experiesFromLMM.length];
		double[] CVADifferenceRelativeToNominal = new double[experiesFromLMM.length];
		
		System.out.println("experies" + "   \t   " + "tenors" + "   \t   " + "cvaLMValue" + "   \t   " + "cvaHWValue" + "   \t   " + "CVADifferenceRelativeToLMM"+ "   \t   " + "CVADifferenceRelativeToNominal");
		for (int i = 0; i < experiesFromLMM.length; i++) {
			

			// create the corresponding swaps
			String swapStart = experiesFromLMM[i];
			String swapTenor = tenorsFromLMM[i];

			StoredSwap swapInitialization = new StoredSwap(swapStart + " " + swapTenor, BuySell, notional, fixedRate, referenceDate,
					swapStart, swapTenor, fixedFrequency, floatFrequency, RateFrequency, fixedCouponConvention,
					xiborCouponConvention);
				swapInitialization.changeToATMswap(LMMCalibrationMaschine.getForwardCurve(), LMMCalibrationMaschine.getCurveModel());
			Swap swap = swapInitialization.getSwap();
			

			
			
			CVA cvaLM = new CVA(LMsimulationModel, swap, recoveryRate, cdsSpreads, LMMCalibrationMaschine.getDiscountCurve());
			CVA cvaHW = new CVA(HWsimulationModel, swap, recoveryRate, cdsSpreads, HWCalibrationMaschine.getDiscountCurve());
			double cvaLMValue;
			double cvaHWValue;
			try {
				cvaLMValue = cvaLM.getValue();
				cvaHWValue = cvaHW.getValue();
			} catch (Exception e) {
				continue;
			}			
			
			
			System.out.print(experiesFromLMM[i] + "\t" + tenorsFromLMM[i] + "\t");
			System.out.print(cvaLMValue + "\t" + cvaHWValue + "\t");
			
			
			CVADifferenceRelativeToLMM[i] = (cvaLMValue - cvaHWValue)/cvaLMValue;
			CVADifferenceRelativeToNominal[i] = (cvaLMValue - cvaHWValue)/notional;
			
			System.out.println(CVADifferenceRelativeToLMM[i] + "\t" + CVADifferenceRelativeToNominal[i]);
		}



	}

}
