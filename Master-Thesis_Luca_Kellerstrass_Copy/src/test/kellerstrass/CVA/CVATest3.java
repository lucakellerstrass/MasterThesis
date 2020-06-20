
package kellerstrass.CVA;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import kellerstrass.CVA.CVA;
import kellerstrass.ModelCalibration.LmmCalibrationMachine;
import kellerstrass.exposure.ExposureMachine;
import kellerstrass.marketInformation.CalibrationInformation;
import kellerstrass.marketInformation.DataScope;
import kellerstrass.marketInformation.DataSource;
import kellerstrass.swap.StoredSwap;
import kellerstrass.useful.StringToUseful;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.BrownianMotionLazyInit;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.Swap;
import net.finmath.montecarlo.interestrate.products.TermStructureMonteCarloProduct;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * Communication class for the Python GUI
 * 
 * @author lucak
 *
 */
public class CVATest3 {
	private static final NumberFormat formatter2 = new DecimalFormat("0.00",
			new DecimalFormatSymbols(new Locale("en")));
	private static final NumberFormat formatter6 = new DecimalFormat("0.000000",
			new DecimalFormatSymbols(new Locale("en")));
	private static DecimalFormat formatterValue = new DecimalFormat(" ##0.00000;-##0.00000",
			new DecimalFormatSymbols(Locale.ENGLISH));

	public static ArrayList<Map<String, String>> main(String SwapName, String BuySell, int notional, double fixedRate,
			String swapStart, String swapEnd, String fixedFrequency, String floatFrequency, String RateFrequency,
			String discountCurve, String forecastCurve, String fixedCouponConvention, String xiborCouponConvention,
			String counterpartyName, double recoveryRateInput, double cdsSpread1y, double cdsSpread2y,
			double cdsSpread3y, double cdsSpread4y, double cdsSpread5y, double cdsSpread6y, double cdsSpread7y,
			double cdsSpread8y, double cdsSpread9y, double cdsSpread10y, String referencedate, int numberOfPaths,
			int NumberOfFactorsLMM, int NumberOfFactorsHW, String dataSourceInput, String dataScopeInput,
			String curveModelInput, double Range) throws Exception {
		double[] cdsSpreads10y = new double[] { cdsSpread1y, cdsSpread2y, cdsSpread3y, cdsSpread4y, cdsSpread5y,
				cdsSpread6y, cdsSpread7y, cdsSpread8y, cdsSpread9y, cdsSpread10y };

		/*
		 * System.out.println("--------------PARAMETER---------------------------");
		 * System.out.println("SwapName "+ SwapName); System.out.println("BuySell "+
		 * BuySell); System.out.println("notional "+ Integer.toString(notional));
		 * System.out.println("fixedRate "+ Double.toString(fixedRate));
		 * System.out.println("swapStart "+ swapStart); System.out.println("swapEnd "+
		 * swapEnd); System.out.println("fixedFrequency "+ fixedFrequency);
		 * System.out.println("floatFrequency "+ floatFrequency);
		 * System.out.println("RateFrequency "+ RateFrequency);
		 * System.out.println("discountCurve "+ discountCurve);
		 * System.out.println("forecastCurve "+ forecastCurve);
		 * System.out.println("forecastCurve "+ forecastCurve);
		 * System.out.println("fixedCouponConvention "+ fixedCouponConvention);
		 * System.out.println("xiborCouponConvention "+ xiborCouponConvention);
		 * 
		 * System.out.println("counterpartyName "+ counterpartyName);
		 * System.out.println("recoveryRateInput "+ Double.toString(recoveryRateInput));
		 * System.out.println("cdsSpread1y "+ Double.toString(cdsSpread1y));
		 * System.out.println("cdsSpread2y "+ Double.toString(cdsSpread2y));
		 * System.out.println("cdsSpread3y "+ Double.toString(cdsSpread3y));
		 * System.out.println("cdsSpread4y "+ Double.toString(cdsSpread4y));
		 * System.out.println("cdsSpread5y "+ Double.toString(cdsSpread5y));
		 * System.out.println("cdsSpread6y "+ Double.toString(cdsSpread6y));
		 * System.out.println("cdsSpread7y "+ Double.toString(cdsSpread7y));
		 * System.out.println("cdsSpread8y "+ Double.toString(cdsSpread8y));
		 * System.out.println("cdsSpread9y "+ Double.toString(cdsSpread9y));
		 * System.out.println("cdsSpread10y "+ Double.toString(cdsSpread10y));
		 * System.out.println("referencedate "+ referencedate);
		 * System.out.println("numberOfPaths "+ Integer.toString(numberOfPaths));
		 * System.out.println("NumberOfFactorsLMM "+
		 * Integer.toString(NumberOfFactorsLMM));
		 * System.out.println("NumberOfFactorsHW "+
		 * Integer.toString(NumberOfFactorsHW)); System.out.println("dataSourceInput "+
		 * dataSourceInput); System.out.println("dataScopeInput "+ dataScopeInput);
		 * System.out.println("curveModelInput "+ curveModelInput);
		 * System.out.println("Range "+ Double.toString(Range));
		 * System.out.println("-----------------------------------------");
		 */

		DataScope dataScope = StringToUseful.dataScopeFromString(dataScopeInput);
		DataSource dataSource = StringToUseful.dataSourceFromString(dataSourceInput);
		CalibrationInformation calibrationInformation = new CalibrationInformation(dataScope, dataSource);
		double lastTime = Range;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);
		boolean forcedCalculation = false;
		int numberOfFactors = NumberOfFactorsLMM;
		BrownianMotionLazyInit brownianMotion = new BrownianMotionLazyInit(timeDiscretizationFromArray, numberOfFactors,
				numberOfPaths, 31415);
		EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion,
				EulerSchemeFromProcessModel.Scheme.EULER);
		LmmCalibrationMachine lmmCalibrationMaschine = new LmmCalibrationMachine(numberOfPaths, numberOfFactors,
				calibrationInformation);
		LIBORModelMonteCarloSimulationModel simulationModel = lmmCalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(process, forcedCalculation);
		StoredSwap inputSwap = new StoredSwap(SwapName, BuySell, notional, fixedRate, referencedate, swapStart, swapEnd,
				fixedFrequency, floatFrequency, RateFrequency,/* discountCurve, forecastCurve,*/ fixedCouponConvention,
				xiborCouponConvention);
		Swap Swap2 = inputSwap.getSwap();
		ExposureMachine swapExposureEstimator = new ExposureMachine(Swap2);
		System.out.println("The name of the Model is: " + lmmCalibrationMaschine.getModelName());
		System.out.println("\n We whant to to the exposure paths of the given model an the swap: ");
		ArrayList<Map<String, String>> OutTable = new ArrayList<Map<String, String>>();
		OutTable = CVATest3.printExpectedExposurePaths(swapExposureEstimator, simulationModel, Swap2);
		double recoveryRate = recoveryRateInput;
		double[] cdsSpreads = cdsSpreads10y;
		CVA cva = new CVA(simulationModel, Swap2, recoveryRate, cdsSpreads, lmmCalibrationMaschine.getDiscountCurve());
		System.out.println("The CVA is \t" + formatterValue.format(cva.getValue()));
		HashMap<String, String> OutTableRow = new HashMap<String, String>();
		OutTableRow.put("ModelName", lmmCalibrationMaschine.getModelName());
		OutTableRow.put("CVA", formatterValue.format(cva.getValue()));
		OutTable.add(OutTable.size(), OutTableRow);
		return OutTable;
	}

	private static ArrayList<Map<String, String>> printExpectedExposurePaths(
			TermStructureMonteCarloProduct swapExposureEstimator, LIBORModelMonteCarloSimulationModel simulationModel,
			AbstractLIBORMonteCarloProduct testSwap) throws CalculationException {
		ArrayList<Map<String, String>> OutTable = new ArrayList<Map<String, String>>();
		System.out.println("observationDate  \t   expected positive Exposure  \t   expected negative Exposure");
		int i = 0;
		Iterator iterator = simulationModel.getTimeDiscretization().iterator();
		while (iterator.hasNext()) {
			double observationDate = (Double) iterator.next();
			RandomVariable valuesSwap = testSwap.getValue(observationDate, simulationModel);
			RandomVariable valuesEstimatedExposure = swapExposureEstimator.getValue(observationDate, simulationModel);
			RandomVariable valuesPositiveExposure = valuesSwap.mult(valuesEstimatedExposure
					.choose(new RandomVariableFromDoubleArray(1.0), new RandomVariableFromDoubleArray(0.0)));
			RandomVariable valuesNegativeExposure = valuesSwap.mult(valuesEstimatedExposure
					.choose(new RandomVariableFromDoubleArray(0.0), new RandomVariableFromDoubleArray(1.0)));
			double expectedPositiveExposure = valuesPositiveExposure.getAverage();
			double expectedNegativeExposure = valuesNegativeExposure.getAverage();
			System.out.println(
					String.valueOf(observationDate) + "    \t         " + formatter6.format(expectedPositiveExposure)
							+ "    \t         " + formatter6.format(expectedNegativeExposure));
			HashMap<String, String> OutTableRow = new HashMap<String, String>();
			OutTableRow.put("observationDate", formatter2.format(observationDate));
			OutTableRow.put("expectedPositiveExposure", formatter6.format(expectedPositiveExposure));
			OutTableRow.put("expectedNegativeExposure", formatter6.format(expectedNegativeExposure));
			OutTable.add(i, OutTableRow);
			++i;
		}
		return OutTable;
	}
}
