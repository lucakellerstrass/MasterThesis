package kellerstrass.PythonComunication;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kellerstrass.CVA.CVA;
import kellerstrass.ModelCalibration.CalibrationMachineInterface;
import kellerstrass.ModelCalibration.HWCalibrationMachine;
import kellerstrass.ModelCalibration.LmmCalibrationMachine;
import kellerstrass.exposure.ExposureMachine;
import kellerstrass.marketInformation.CalibrationInformation;
import kellerstrass.marketInformation.CurveModelDataType;
import kellerstrass.marketInformation.DataScope;
import kellerstrass.marketInformation.DataSource;
import kellerstrass.swap.StoredSwap;
import kellerstrass.useful.StringToUseful;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.Swap;
import net.finmath.montecarlo.interestrate.products.TermStructureMonteCarloProduct;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.optimizer.SolverException;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * Class For Communication for the Python GUI
 * 
 * @author lucak
 *
 *
 * @Todo: Complete this class
 */
public class CVAandCalibrationTestForPython {

	private final static NumberFormat formatter2 = new DecimalFormat("0.00",
			new DecimalFormatSymbols(new Locale("en")));
	private final static NumberFormat formatter6 = new DecimalFormat("0.000000",
			new DecimalFormatSymbols(new Locale("en")));
	private static DecimalFormat formatterValue = new DecimalFormat(" ##0.00000;-##0.00000",
			new DecimalFormatSymbols(Locale.ENGLISH));

	/*
	 * private String SwapName; private String BuySell; private int notional;
	 * private double fixedRate; private String swapStart; private String swapEnd;
	 * private String fixedFrequency; private String floatFrequency; private String
	 * RateFrequency; private String discountCurve; private String forecastCurve;
	 * private String fixedCouponConvention; private String xiborCouponConvention;
	 */

	private static boolean forcedCalculation = false;

	static StoredSwap inputSwap;
	private  Swap swap;
	private  TermStructureMonteCarloProduct swapExposureEstimator;
	private CalibrationInformation calibrationInformation;
	private TimeDiscretizationFromArray simulationTimeDiscretization;

//	private static  int NumberOfFactorsHW;
	private  int NumberOfFactorsLmm;
	private  int numberOfPaths;

	private  CalibrationMachineInterface lmmCalibrationmashine;
	private  CalibrationMachineInterface hwCalibrationmashine;
	
	private DataSource dataSource;
	private CurveModelDataType curveModelDataType;
	private String referenceDate;

	private  double recoveryRate;
	private  double[] cdsSpreads10y = new double[10];
	// Counterparty information

	public CVAandCalibrationTestForPython(String SwapName, String BuySell, int notional, double fixedRate,
			String swapStart, String swapEnd, String fixedFrequency, String floatFrequency, String RateFrequency,
			/* String discountCurve, String forecastCurve, */ String fixedCouponConvention,
			String xiborCouponConvention, String counterpartyName, double recoveryRateInput, double cdsSpread1y,
			double cdsSpread2y, double cdsSpread3y, double cdsSpread4y, double cdsSpread5y, double cdsSpread6y,
			double cdsSpread7y, double cdsSpread8y, double cdsSpread9y, double cdsSpread10y,

			// Modelling parameters
			String referencedate, int numberOfPaths, int NumberOfFactorsLMM, /*
																				 * int NumberOfFactorsHW should be
																				 * deleted,
																				 */
			String dataSourceInput, String dataScopeInput, String curveModelInput, double Range

	) throws SolverException {

		this.recoveryRate = recoveryRateInput;
		// Fill in the cdsSpread array
		this.cdsSpreads10y[0] = cdsSpread1y;
		this.cdsSpreads10y[1] = cdsSpread2y;
		this.cdsSpreads10y[2] = cdsSpread3y;
		this.cdsSpreads10y[3] = cdsSpread4y;
		this.cdsSpreads10y[4] = cdsSpread5y;
		this.cdsSpreads10y[5] = cdsSpread6y;
		this.cdsSpreads10y[6] = cdsSpread7y;
		this.cdsSpreads10y[7] = cdsSpread8y;
		this.cdsSpreads10y[8] = cdsSpread9y;
		this.cdsSpreads10y[9] = cdsSpread10y;

		/*
		 * this.SwapName = SwapName; this.BuySell = BuySell; this.notional =notional;
		 * this.fixedRate = fixedRate; this.swapStart = swapStart; this.swapEnd =
		 * swapEnd; this.fixedFrequency = fixedFrequency; this.floatFrequency =
		 * floatFrequency; this.RateFrequency = RateFrequency; this.discountCurve =
		 * discountCurve; this.forecastCurve = forecastCurve; this.fixedCouponConvention
		 * = fixedCouponConvention; this.xiborCouponConvention = xiborCouponConvention;
		 */

		DataScope dataScope = StringToUseful.dataScopeFromString(dataScopeInput);
		this.dataSource = StringToUseful.dataSourceFromString(dataSourceInput);
		this.calibrationInformation = new CalibrationInformation(dataScope, dataSource);

		this.curveModelDataType = StringToUseful.getCurveModelDataTypeFromString(curveModelInput);

//		this.NumberOfFactorsHW = NumberOfFactorsHW;
		this.NumberOfFactorsLmm = NumberOfFactorsLMM;
		this.numberOfPaths = numberOfPaths;

		
		double lastTime = 40.0;
		double dt = 0.25;
		this.simulationTimeDiscretization = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);

		// Initialization
		this.lmmCalibrationmashine = new LmmCalibrationMachine(numberOfPaths,
				NumberOfFactorsLMM, calibrationInformation, curveModelDataType);
		// Initialization
		this.hwCalibrationmashine = new HWCalibrationMachine(numberOfPaths,
				2 /* NumberOfFactorsHW */, calibrationInformation, curveModelDataType);

		this.referenceDate = referencedate;
		CVAandCalibrationTestForPython.inputSwap = new StoredSwap(SwapName, BuySell, notional, fixedRate, referencedate,
				swapStart, swapEnd, fixedFrequency, floatFrequency, RateFrequency/* , discountCurve, forecastCurve */,
				fixedCouponConvention, xiborCouponConvention);
		
		if (fixedRate == -1.0) {
		// do ATM
		inputSwap.changeToATMswap(hwCalibrationmashine.getForwardCurve(), hwCalibrationmashine.getCurveModel());
		}
		
		
		this.swap = inputSwap.getSwap();
		this.swapExposureEstimator = new ExposureMachine(swap);

	}

	/**
	 * Print the exposure paths for the Libor market model
	 * 
	 * @return A HashMap of Exposure paths with the rows: observationDate,
	 *         expectedPositiveExposure, expectedNegativeExposure
	 * @throws Exception
	 */
	public List<Map<String, String>> printExpectedExposurePathsLmm() throws Exception {
		return printExpectedExposurePaths(NumberOfFactorsLmm, lmmCalibrationmashine);
	}

	/**
	 * Print the exposure paths for the Hull White model
	 * 
	 * @return A HashMap of Exposure paths with the columns: observationDate,
	 *         expectedPositiveExposure, expectedNegativeExposure
	 * @throws Exception
	 */
	public List<Map<String, String>> printExpectedExposurePathsHw() throws Exception {
		return printExpectedExposurePaths(2 /* NumberOfFactorsHW */, hwCalibrationmashine);
	}

	/**
	 * This method returns the calibration results of the LIBOR Market Model with
	 * the corresponding calibration quality <br>
	 * The columns are: <br>
	 * Calibration Item: Some String that is the name of the Calibration Product
	 * <br>
	 * Model_Value: The value of these calibration product, the model calculates
	 * after calibration <br>
	 * Target: The target value. The value "Model_Value" should hit. <br>
	 * Deviation: the difference between Model value and target.
	 * 
	 * @return
	 */
	public  ArrayList<Map<String, Object>> printCalibrationTestLmm() {
		return lmmCalibrationmashine.getCalibrationTable(forcedCalculation);
	}

	/**
	 * This method returns the calibration results of the Hull White Model with the
	 * corresponding calibration quality <br>
	 * The columns are: <br>
	 * Calibration Item: Some String that is the name of the Calibration Product
	 * <br>
	 * Model_Value: The value of the calibration product, the model calculates after
	 * calibration <br>
	 * Target: The target value. The value "Model_Value" should hit. <br>
	 * Deviation: the difference between Model value and target.
	 * 
	 * @return
	 */
	public  ArrayList<Map<String, Object>> printCalibrationTestHw() {
		return hwCalibrationmashine.getCalibrationTable(forcedCalculation);
	}

	/**
	 * Returns a List<Map<String, String>> with the exposure paths, model name and
	 * CVA
	 * 
	 * @param numberOFFactors
	 * @param CalibrationMachine
	 * @return
	 * @throws Exception
	 */
	private List<Map<String, String>> printExpectedExposurePaths(int numberOFFactors,
			CalibrationMachineInterface CalibrationMachine) throws Exception {


		// brownian motion
		BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(simulationTimeDiscretization,
				numberOFFactors, numberOfPaths, 31415 /* seed */);
		// process
		EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion,
				EulerSchemeFromProcessModel.Scheme.EULER);

		// simulation machine
		LIBORModelMonteCarloSimulationModel simulationModel = CalibrationMachine
				.getLIBORModelMonteCarloSimulationModel(process, forcedCalculation);

		System.out.println("The name of the Model is: " + CalibrationMachine.getModelName());
		System.out.println(
				"\n We want to see the exposure paths of the given model an the swap: " + inputSwap.getSwapName());

		// OutTable for return for Python
		List<Map<String, String>> OutTable = new ArrayList<Map<String, String>>();

		System.out.println("observationDate  \t   expected positive Exposure  \t   expected negative Exposure");
		int i = 0;
		double endObservationDate = 0;
		for (double observationDate : simulationModel.getTimeDiscretization()) {

			/*
			 * Calculate expected positive exposure of a swap
			 */
			RandomVariable valuesSwap;

			// To control the value for t=0.
			if (observationDate == 0) {
				valuesSwap = new RandomVariableFromDoubleArray(0);
			} else {
				valuesSwap = swap.getValue(observationDate, simulationModel);
				if (swap.getValue(observationDate, simulationModel).getAverage() == 0) {
					continue;
				}
			}

			RandomVariable valuesEstimatedExposure = swapExposureEstimator.getValue(observationDate, simulationModel);
			RandomVariable valuesPositiveExposure = valuesSwap.mult(valuesEstimatedExposure
					.choose(new RandomVariableFromDoubleArray(1.0), new RandomVariableFromDoubleArray(0.0)));
			RandomVariable valuesNegativeExposure = valuesSwap.mult(valuesEstimatedExposure
					.choose(new RandomVariableFromDoubleArray(0.0), new RandomVariableFromDoubleArray(1.0)));

			double expectedPositiveExposure = valuesPositiveExposure.getAverage();
			double expectedNegativeExposure = valuesNegativeExposure.getAverage();

			System.out.println(observationDate + "    \t         " + formatter6.format(expectedPositiveExposure)
					+ "    \t         " + formatter6.format(expectedNegativeExposure));

			endObservationDate = observationDate;
			Map<String, String> OutTableRow = new HashMap<>();
			OutTableRow.put("observationDate", formatter2.format(observationDate));
			OutTableRow.put("expectedPositiveExposure", formatter6.format(expectedPositiveExposure));
			OutTableRow.put("expectedNegativeExposure", formatter6.format(expectedNegativeExposure));
			OutTable.add(i, OutTableRow);
			i++;

		}
		
		
		endObservationDate = endObservationDate + 0.25;
		
		//Print out one more time where everything is zero
		Map<String, String> OutTableRow2 = new HashMap<>();
		OutTableRow2.put("observationDate", formatter2.format(endObservationDate));
		OutTableRow2.put("expectedPositiveExposure", formatter6.format(0.0));
		OutTableRow2.put("expectedNegativeExposure", formatter6.format(0.0));
		OutTable.add(i, OutTableRow2);
		
		CVA cva = new CVA(simulationModel, swap, recoveryRate, cdsSpreads10y, CalibrationMachine.getDiscountCurve());

		Map<String, String> OutTableRow = new HashMap<>();
		OutTableRow.put("ModelName", CalibrationMachine.getModelName());
		OutTableRow.put("CVA", formatterValue.format(cva.getValue()));
		//OutTableRow.put("CVA", cva.getValue());
		OutTable.add(i+1, OutTableRow);
		return OutTable;

	}
	
	
	
	public List<Map<String, Object>> printCVAComparismUnderSwapMatrix() throws Exception {

		// Set the Calibration basket
				CalibrationInformation calibrationInformationLMM = new CalibrationInformation(DataScope.FullSurface,
						dataSource);
				CalibrationInformation calibrationInformationHW = new CalibrationInformation(DataScope.CoTerminals, dataSource);
		
		
				// Simulation time discretization
				double lastTime = 40.0;
				double dt = 0.25;
				TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
						(int) (lastTime / dt), dt);
		
		
				// brownian motion
				BrownianMotion brownianMotionLMM = new net.finmath.montecarlo.BrownianMotionLazyInit(
						timeDiscretizationFromArray, NumberOfFactorsLmm, numberOfPaths, 31415 /* seed */);
				BrownianMotion brownianMotionHW = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
						2 /*numberOfFactors*/, numberOfPaths, 31415 /* seed */);

				// process
				EulerSchemeFromProcessModel processLMM = new EulerSchemeFromProcessModel(brownianMotionLMM,
						EulerSchemeFromProcessModel.Scheme.EULER);
				EulerSchemeFromProcessModel processHW = new EulerSchemeFromProcessModel(brownianMotionHW,
						EulerSchemeFromProcessModel.Scheme.EULER);

				// calibration machine
				CalibrationMachineInterface LMMCalibrationMaschine = new LmmCalibrationMachine(numberOfPaths, NumberOfFactorsLmm,
						calibrationInformationLMM, curveModelDataType);
				CalibrationMachineInterface HWCalibrationMaschine = new HWCalibrationMachine(numberOfPaths, 2 /*numberOfFactors*/,
						calibrationInformationHW, curveModelDataType);

				// simulation models
				LIBORModelMonteCarloSimulationModel LMsimulationModel = LMMCalibrationMaschine
						.getLIBORModelMonteCarloSimulationModel(processLMM, forcedCalculation);
				LIBORModelMonteCarloSimulationModel HWsimulationModel = HWCalibrationMaschine
						.getLIBORModelMonteCarloSimulationModel(processHW, forcedCalculation);
		
		
		
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
//				double[] CVADifferenceRelativeToNominal = new double[experiesFromLMM.length];
				
//				System.out.println("experies" + "   \t   " + "tenors" + "   \t   " + "cvaLMValue" + "   \t   " + "cvaHWValue" + "   \t   " + "CVADifferenceRelativeToLMM");//+ "   \t   " + "CVADifferenceRelativeToNominal");
				
				
				// OutTable for return for Python
				List<Map<String, Object>> OutTable = new ArrayList<Map<String, Object>>();
				
				int j = 0;
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
					Map<String, Object> OutTableRow = new HashMap<>();
					OutTableRow.put("experies", experiesFromLMM[i]);
					OutTableRow.put("tenors", tenorsFromLMM[i]);
					OutTableRow.put("cvaValueLMM", formatter2.format(cvaLMValue));
					OutTableRow.put("cvaValueHW", formatter2.format(cvaHWValue));
					CVADifferenceRelativeToLMM[i] = Math.abs((cvaLMValue - cvaHWValue)/cvaLMValue);
					OutTableRow.put("CVADifferenceRelativeToLMM", formatter6.format(CVADifferenceRelativeToLMM[i]));
					
					
					
					
				}
				
				
		return OutTable;

	}
	
	
	
	

}
