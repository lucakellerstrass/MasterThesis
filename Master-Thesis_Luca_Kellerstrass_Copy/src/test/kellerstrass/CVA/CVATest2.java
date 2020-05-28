package kellerstrass.CVA;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kellerstrass.exposure.ExposureMaschine;
import kellerstrass.marketInformation.CalibrationInformation;
import kellerstrass.marketInformation.DataScope;
import kellerstrass.marketInformation.DataSource;
import kellerstrass.swap.StoredSwap;
import kellerstrass.useful.StringToUseful;
import kellerstrass.ModelCalibration.CalibrationMaschineInterface;
import kellerstrass.ModelCalibration.LmmCalibrationMaschine;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.Swap;
import net.finmath.montecarlo.interestrate.products.TermStructureMonteCarloProduct;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretizationFromArray;

public class CVATest2 {

	private final static NumberFormat formatter2 = new DecimalFormat("0.00",
			new DecimalFormatSymbols(new Locale("en")));
	private final static NumberFormat formatter6 = new DecimalFormat("0.000000",
			new DecimalFormatSymbols(new Locale("en")));
	private static DecimalFormat formatterValue = new DecimalFormat(" ##0.00000;-##0.00000",
			new DecimalFormatSymbols(Locale.ENGLISH));

	public static List<Map<String, String>> main(String SwapName, String BuySell, int notional, double fixedRate,
			String swapStart, String swapEnd, String fixedFrequency, String floatFrequency, String RateFrequency,
			String discountCurve, String forecastCurve, String fixedCouponConvention, String xiborCouponConvention,
			// Counterparty information
			String counterpartyName, double recoveryRateInput, double cdsSpread1y, double cdsSpread2y,
			double cdsSpread3y, double cdsSpread4y, double cdsSpread5y, double cdsSpread6y, double cdsSpread7y,
			double cdsSpread8y, double cdsSpread9y, double cdsSpread10y,

			// Modelling parameters
			String referencedate, int numberOfPaths, int NumberOfFactorsLMM, int NumberOfFactorsHW,
			String dataSourceInput, String dataScopeInput, String curveModelInput, double Range

	) throws Exception {

		double[] cdsSpreads10y = { cdsSpread1y, cdsSpread2y, cdsSpread3y, cdsSpread4y, cdsSpread5y, cdsSpread6y,
				cdsSpread7y, cdsSpread8y, cdsSpread9y, cdsSpread10y };

		// test of everything is read in correct
		/*
		 * System.out.println("SwapName is: " + SwapName);
		 * System.out.println("Buy/Sell is: " + BuySell);
		 * System.out.println("notional is: " + notional);
		 * System.out.println("fixedRate is: " + fixedRate);
		 * System.out.println("swapStart is: " + swapStart);
		 * System.out.println("swapEnd is: " + swapEnd);
		 * System.out.println("fixedFrequency is: " + fixedFrequency);
		 * System.out.println("floatFrequancy is: " + floatFrequency);
		 * System.out.println("RateFrequency is: " + RateFrequency);
		 * System.out.println("discountCurve is: " + discountCurve);
		 * System.out.println("forecastCurve is: " + forecastCurve);
		 * System.out.println("fixedCouponConvention is: " + fixedCouponConvention);
		 * System.out.println("xiborCouponConvention is: " + xiborCouponConvention);
		 * System.out.println(""); System.out.println("counterpartyName is: " +
		 * counterpartyName); System.out.println("recoveryRate is: " +
		 * recoveryRateInput); System.out.println("cdsSpreads10y is: (" + cdsSpread1y +
		 * ", " + cdsSpread2y + ", " + cdsSpread3y + ", " + cdsSpread4y + ", " +
		 * cdsSpread5y + ", " + cdsSpread6y + ", " + cdsSpread7y + ", " + cdsSpread8y +
		 * ", " + cdsSpread9y + ", " + cdsSpread10y + ")");
		 * System.out.println("referencedate is: " + referencedate);
		 * System.out.println("numberOfPaths is: " + numberOfPaths);
		 * System.out.println("NumberOfFactorsLMM is: " + NumberOfFactorsLMM);
		 * System.out.println("NumberOfFactorsHW is: " + NumberOfFactorsHW);
		 * System.out.println("dataSource is: " + dataSourceInput);
		 * System.out.println("dataScope is: " + dataScopeInput);
		 * System.out.println("curveModel is: " + curveModelInput);
		 * System.out.println("Range is: " + Range);
		 */

		// Set the Calibration set. Here: e.g. Example Co-Terminals

		DataScope dataScope = StringToUseful.dataScopeFromString(dataSourceInput);
		DataSource dataSource = StringToUseful.dataSourceFromString(dataSourceInput);

		// Set the Calibration set. Here: e.g. Example Co-Terminals

		CalibrationInformation calibrationInformation = new CalibrationInformation(dataScope, dataSource);

		// Simulation time discretization
		double lastTime = Range;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);

		boolean forcedCalculation = false;

		int numberOfFactors = NumberOfFactorsLMM;

		// brownian motion
		BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				numberOfFactors, numberOfPaths, 31415 /* seed */);
		// process
		EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion,
				EulerSchemeFromProcessModel.Scheme.EULER);
		// calibration machine
		CalibrationMaschineInterface lmmCalibrationMaschine = new LmmCalibrationMaschine(numberOfPaths, numberOfFactors,
				calibrationInformation);
		// simulation machine
		LIBORModelMonteCarloSimulationModel simulationModel = lmmCalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(process, forcedCalculation);

		// Swap

		StoredSwap inputSwap = new StoredSwap(SwapName, BuySell, notional, fixedRate, referencedate, swapStart, swapEnd,
				fixedFrequency, floatFrequency, RateFrequency, discountCurve, forecastCurve, fixedCouponConvention,
				xiborCouponConvention);

		Swap Swap = inputSwap.getSwap();

		// Exposure Maschine
		// ExposureMaschine exposureMaschine = new ExposureMaschine(testSwap);
		TermStructureMonteCarloProduct swapExposureEstimator = new ExposureMaschine(Swap);

		System.out.println("The name of the Model is: " + lmmCalibrationMaschine.getModelName());

		System.out.println("\n We whant to to the exposure paths of the given model an the swap: " /*
																									 * + testStoredSwap.
																									 * getSwapName()
																									 */);

		List<Map<String, String>> OutTable = new ArrayList<Map<String, String>>();
		OutTable = printExpectedExposurePaths(swapExposureEstimator, simulationModel, Swap);

		double recoveryRate = recoveryRateInput; // 0.4;

		double[] cdsSpreads = cdsSpreads10y;// { 300.0, 350.0, 400.0, 450.0, 500.0, 550.0, 600.0, 650.0, 700.0, 750.0 };

		CVA cva = new CVA(simulationModel, Swap, recoveryRate, cdsSpreads, lmmCalibrationMaschine.getDiscountCurve());

		System.out.println("The CVA is \t" + formatterValue.format(cva.getValue()));

		return OutTable;
	}

	private static List<Map<String, String>> printExpectedExposurePaths(
			TermStructureMonteCarloProduct swapExposureEstimator, LIBORModelMonteCarloSimulationModel simulationModel,
			AbstractLIBORMonteCarloProduct testSwap) throws CalculationException {

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
			RandomVariable valuesSwap = testSwap.getValue(observationDate, simulationModel);
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
		return OutTable;
	}

}