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
import kellerstrass.marketInformation.DataScope;
import kellerstrass.marketInformation.DataSource;
import kellerstrass.swap.StoredSwap;
import kellerstrass.useful.StringToUseful;
import net.finmath.exception.CalculationException;
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
	private static  Swap swap;
	private static  TermStructureMonteCarloProduct swapExposureEstimator;
	private  CalibrationInformation calibrationInformation;
	private static  TimeDiscretizationFromArray simulationTimeDiscretization;

	private static  int NumberOfFactorsHW;
	private static  int NumberOfFactorsLmm;
	private static  int numberOfPaths;

	private static  CalibrationMachineInterface lmmCalibrationmashine;
	private static  CalibrationMachineInterface hwCalibrationmashine;

	private static  double recoveryRate;
	private static  double[] cdsSpreads10y = new double[10];
	// Counterparty information

	public CVAandCalibrationTestForPython(String SwapName, String BuySell, int notional, double fixedRate,
			String swapStart, String swapEnd, String fixedFrequency, String floatFrequency, String RateFrequency,
			String discountCurve, String forecastCurve, String fixedCouponConvention, String xiborCouponConvention,
			String counterpartyName, double recoveryRateInput, double cdsSpread1y, double cdsSpread2y,
			double cdsSpread3y, double cdsSpread4y, double cdsSpread5y, double cdsSpread6y, double cdsSpread7y,
			double cdsSpread8y, double cdsSpread9y, double cdsSpread10y,

			// Modelling parameters
			String referencedate, int numberOfPaths, int NumberOfFactorsLMM, int NumberOfFactorsHW,
			String dataSourceInput, String dataScopeInput, String curveModelInput, double Range

	) {

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

		this.inputSwap = new StoredSwap(SwapName, BuySell, notional, fixedRate, referencedate, swapStart, swapEnd,
				fixedFrequency, floatFrequency, RateFrequency, discountCurve, forecastCurve, fixedCouponConvention,
				xiborCouponConvention);
		this.swap = inputSwap.getSwap();
		this.swapExposureEstimator = new ExposureMachine(swap);

		DataScope dataScope = StringToUseful.dataScopeFromString(dataScopeInput);
		DataSource dataSource = StringToUseful.dataSourceFromString(dataSourceInput);

		this.calibrationInformation = new CalibrationInformation(dataScope, dataSource);

		this.NumberOfFactorsHW = NumberOfFactorsHW;
		this.NumberOfFactorsLmm = NumberOfFactorsLMM;
		this.numberOfPaths = numberOfPaths;

		double dt = 0.25;
		this.simulationTimeDiscretization = new TimeDiscretizationFromArray(0.0, (int) (Range / dt), dt);

		// Initialization
		this.lmmCalibrationmashine = new LmmCalibrationMachine(numberOfPaths, NumberOfFactorsLMM,
				calibrationInformation);
		// Initialization
		this.hwCalibrationmashine = new HWCalibrationMachine(numberOfPaths, NumberOfFactorsHW, calibrationInformation);

	}

	/**
	 * Print the exposure paths for the Libor market model
	 * 
	 * @return A HashMap of Exposure paths with the rows: observationDate,
	 *         expectedPositiveExposure, expectedNegativeExposure
	 * @throws Exception 
	 */
	public static List<Map<String, String>> printExpectedExposurePathsLmm()
			throws Exception {
		return printExpectedExposurePaths(NumberOfFactorsLmm, lmmCalibrationmashine);
	}

	/**
	 * Print the exposure paths for the Hull White model
	 * 
	 * @return A HashMap of Exposure paths with the columns: observationDate,
	 *         expectedPositiveExposure, expectedNegativeExposure
	 * @throws Exception 
	 */
	public static List<Map<String, String>> printExpectedExposurePathsHw()
			throws Exception {
		return printExpectedExposurePaths(NumberOfFactorsHW, hwCalibrationmashine);
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
	public static ArrayList<Map<String, Object>> printCalibrationTestLmm() {
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
	public static ArrayList<Map<String, Object>> printCalibrationTestHw() {
		return hwCalibrationmashine.getCalibrationTable(forcedCalculation);
	}

	
	
	
	/**
	 * Returns a List<Map<String, String>> with the exposure paths, model name and CVA
	 * @param numberOFFactors
	 * @param CalibrationMachine
	 * @return
	 * @throws Exception 
	 */
	private static List<Map<String, String>> printExpectedExposurePaths(int numberOFFactors,
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
		for (double observationDate : simulationModel.getTimeDiscretization()) {

			/*
			 * if(observationDate == 0) { continue; }
			 */

			/*
			 * Calculate expected positive exposure of a swap
			 */
			RandomVariable valuesSwap = swap.getValue(observationDate, simulationModel);
			RandomVariable valuesEstimatedExposure = swapExposureEstimator.getValue(observationDate, simulationModel);
			RandomVariable valuesPositiveExposure = valuesSwap.mult(valuesEstimatedExposure
					.choose(new RandomVariableFromDoubleArray(1.0), new RandomVariableFromDoubleArray(0.0)));
			RandomVariable valuesNegativeExposure = valuesSwap.mult(valuesEstimatedExposure
					.choose(new RandomVariableFromDoubleArray(0.0), new RandomVariableFromDoubleArray(1.0)));

			double expectedPositiveExposure = valuesPositiveExposure.getAverage();
			double expectedNegativeExposure = valuesNegativeExposure.getAverage();

			System.out.println(observationDate + "    \t         " + formatter6.format(expectedPositiveExposure)
					+ "    \t         " + formatter6.format(expectedNegativeExposure));

			Map<String, String> OutTableRow = new HashMap<>();
			OutTableRow.put("observationDate", formatter2.format(observationDate));
			OutTableRow.put("expectedPositiveExposure", formatter6.format(expectedPositiveExposure));
			OutTableRow.put("expectedNegativeExposure", formatter6.format(expectedNegativeExposure));
			OutTable.add(i, OutTableRow);
			i++;

		}
		CVA cva = new CVA(simulationModel, swap, recoveryRate, cdsSpreads10y, CalibrationMachine.getDiscountCurve());
		
		Map<String, String> OutTableRow = new HashMap<>();
		OutTableRow.put("ModelName", CalibrationMachine.getModelName());
		OutTableRow.put("CVA", formatterValue.format(cva.getValue()));
		OutTable.add(i, OutTableRow);
		return OutTable;

	}

}
