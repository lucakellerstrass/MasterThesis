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
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretizationFromArray;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;
import net.finmath.time.daycount.DayCountConvention;

public class CVAComparismExperiment {

	private final static NumberFormat formatter6 = new DecimalFormat("0.000000",
			new DecimalFormatSymbols(new Locale("en")));
	private static DecimalFormat formatterValue = new DecimalFormat(" ##0.00000;-##0.00000",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation = new DecimalFormat(" 0.00000E00;-0.00000E00",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterPercentage = new DecimalFormat(" ##0.000%;-##0.000%",
			new DecimalFormatSymbols(Locale.ENGLISH));

	public static void main(String[] args) throws Exception {
		boolean forcedCalculation = true;

		// First we calibrate the Hull White Model

		CalibrationInformation calibrationInformationForHw = new CalibrationInformation(DataScope.FullSurface,
				DataSource.EXAMPLE);

		CurveModelCalibrationMachine curveModelCalibrationMaschine = new CurveModelCalibrationMachine(
				CurveModelDataType.Example);

		int numberOfPaths = 1000;

		int numberOfFactorsHw = 2; // For Hull white Model

		// Simulation time discretization
		double lastTime = 40.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);
		// brownian motion
		BrownianMotion brownianMotionHw = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				numberOfFactorsHw, numberOfPaths, 31415 /* seed */);
		// process
		EulerSchemeFromProcessModel processHw = new EulerSchemeFromProcessModel(brownianMotionHw,
				EulerSchemeFromProcessModel.Scheme.EULER);

		CalibrationMachineInterface HwCalibrationMaschine = new HWCalibrationMachine(numberOfPaths, numberOfFactorsHw,
				calibrationInformationForHw, curveModelCalibrationMaschine);

		LIBORModelMonteCarloSimulationModel HwModel = HwCalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(processHw, forcedCalculation);

		
		
		// We want to use the Hull White Model calibrated swaption volatilities to
		// calibrate the LIBOR Market Model
		double swapPeriodLength = calibrationInformationForHw.getSwapPeriodLength();
		String targetVolatilityType = calibrationInformationForHw.getTargetVolatilityType();
		LocalDate referenceDate = calibrationInformationForHw.getReferenceDate();
		BusinessdayCalendarExcludingTARGETHolidays cal = calibrationInformationForHw.getCal();
		DayCountConvention modelDC = calibrationInformationForHw.getModelDC();
		String DataName = calibrationInformationForHw.getName() + " from HullWhite";

		ArrayList<Map<String, Object>> calibrationTable = HwCalibrationMaschine.getCalibrationTable(forcedCalculation);
		String[] atmExpiries = new String[calibrationTable.size() - 1];
		String[] atmTenors = new String[calibrationTable.size() - 1];
		double[] atmVolatilitiesByHw = new double[calibrationTable.size() - 1];

		for (int i = 0; i < calibrationTable.size() - 1; i++) {
			atmExpiries[i] = (String) calibrationTable.get(i).get("Expiry");
			atmTenors[i] = (String) calibrationTable.get(i).get("Tenor");
			atmVolatilitiesByHw[i] = (double) calibrationTable.get(i).get("Model_Value");
		}

		CalibrationInformation calibrationInformationForLmm = new CalibrationInformation(swapPeriodLength, atmExpiries,
				atmTenors, atmVolatilitiesByHw, targetVolatilityType, referenceDate, cal, modelDC, DataName);

		int numberOfFactorsLmm = 3; // For Libor Market Model
		BrownianMotion brownianMotionLmm = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				numberOfFactorsLmm, numberOfPaths, 31415 /* seed */);
		
		
		EulerSchemeFromProcessModel processLmm = new EulerSchemeFromProcessModel(brownianMotionLmm,
				EulerSchemeFromProcessModel.Scheme.EULER);
		// calibration machine
		
		
		CalibrationMachineInterface LmmCalibrationMaschine = new LmmCalibrationMachine(numberOfPaths,
				numberOfFactorsLmm, calibrationInformationForLmm, curveModelCalibrationMaschine);
		
		// simulation model
		LIBORModelMonteCarloSimulationModel LiborMarketModel = LmmCalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(processLmm, forcedCalculation);
		
				

		// Swap
		StoredSwap testStoredSwap = new StoredSwap("Example 2");
		Swap testSwap = testStoredSwap.getSwap();

		double recoveryRate = 0.4;

		double[] cdsSpreads = { 300.0, 350.0, 400.0, 450.0, 500.0, 550.0, 600.0, 650.0, 700.0, 750.0 };

		CVA cvaHw = new CVA(HwModel, testSwap, recoveryRate, cdsSpreads, HwCalibrationMaschine.getDiscountCurve());
		CVA cvaLmm = new CVA(LiborMarketModel, testSwap, recoveryRate, cdsSpreads, LmmCalibrationMaschine.getDiscountCurve());
		double cvaValueHw = cvaHw.getValue();
		double cvaValueLmm = cvaLmm.getValue();

		// Exposure Maschine
		// ExposureMaschine exposureMaschine = new ExposureMaschine(testSwap);
		TermStructureMonteCarloProduct swapExposureEstimator = new ExposureMachine(testSwap);

		System.out.println("\n We want to compare the given models");
		System.out.println("Model 1 is: " + HwCalibrationMaschine.getModelName());
		System.out.println("Model 2 is: " + LmmCalibrationMaschine.getModelName() + "\n");

		System.out.println("The CVA with Model 1 is \t" + formatterValue.format(cvaValueHw));
		System.out.println("The CVA with Model 2 is \t" + formatterValue.format(cvaValueLmm));
		System.out.println("The deviation (CVA1 - CVA2) is: \t" + formatterValue.format(cvaValueHw - cvaValueLmm)
				+ ", which is " + formatterPercentage.format((cvaValueHw - cvaValueLmm) / cvaValueHw) + "\n");

		// Print the Calibration Tests for two two models
		HwCalibrationMaschine.printCalibrationTest();
		
		LmmCalibrationMaschine.printCalibrationTest();

		printExpectedExposurePathsCpmarism(swapExposureEstimator, HwModel, LiborMarketModel, testSwap);

	}

	private static void printExpectedExposurePathsCpmarism(TermStructureMonteCarloProduct swapExposureEstimator,
			LIBORModelMonteCarloSimulationModel Model1, LIBORModelMonteCarloSimulationModel Model2,
			AbstractLIBORMonteCarloProduct testSwap) throws CalculationException {
		System.out.println(
				"observationDate  \t Model 1: \t   expected positive Exposure   \t   expected negative Exposure "
						+ "\t Model 2: \t   expected positive Exposure   \t   expected negative Exposure"
						+ "\t deviation Model 1 - Model 2: \t   expected positive Exposure   \t   expected negative Exposure");
		for (double observationDate : Model1.getTimeDiscretization()) {

			/*
			 * if(observationDate == 0) { continue; }
			 */

			
			 //Calculate expected positive exposure of a swap
			
			// Model 1
			RandomVariable valuesSwapM1 = testSwap.getValue(observationDate, Model1);
			RandomVariable valuesEstimatedExposureM1 = swapExposureEstimator.getValue(observationDate, Model1);
			RandomVariable valuesPositiveExposureM1 = valuesSwapM1.mult(valuesEstimatedExposureM1
					.choose(new RandomVariableFromDoubleArray(1.0), new RandomVariableFromDoubleArray(0.0)));
			RandomVariable valuesNegativeExposureM1 = valuesSwapM1.mult(valuesEstimatedExposureM1
					.choose(new RandomVariableFromDoubleArray(0.0), new RandomVariableFromDoubleArray(1.0)));
			double expectedPositiveExposureM1 = valuesPositiveExposureM1.getAverage();
			double expectedNegativeExposureM1 = valuesNegativeExposureM1.getAverage();

			// Model 2
			RandomVariable valuesSwapM2 = testSwap.getValue(observationDate, Model2);
			RandomVariable valuesEstimatedExposureM2 = swapExposureEstimator.getValue(observationDate, Model2);
			RandomVariable valuesPositiveExposureM2 = valuesSwapM2.mult(valuesEstimatedExposureM2
					.choose(new RandomVariableFromDoubleArray(1.0), new RandomVariableFromDoubleArray(0.0)));
			RandomVariable valuesNegativeExposureM2 = valuesSwapM2.mult(valuesEstimatedExposureM2
					.choose(new RandomVariableFromDoubleArray(0.0), new RandomVariableFromDoubleArray(1.0)));
			double expectedPositiveExposureM2 = valuesPositiveExposureM2.getAverage();
			double expectedNegativeExposureM2 = valuesNegativeExposureM2.getAverage();

			double expectedPositiveExposureDeviation = expectedPositiveExposureM1 - expectedPositiveExposureM2;
			double expectedNegativeExposureDeviation = expectedNegativeExposureM1 - expectedNegativeExposureM2;

			System.out.println(observationDate + "    \t                                   \t"
					+ formatter6.format(expectedPositiveExposureM1) + "    \t                        "
					+ formatter6.format(expectedNegativeExposureM1) + "\t                                   \t"
					+ formatter6.format(expectedPositiveExposureM2) + "    \t                        "
					+ formatter6.format(expectedNegativeExposureM2) + "\t                                   \t"
					+ formatter6.format(expectedPositiveExposureDeviation) + "    \t                        "
					+ formatter6.format(expectedNegativeExposureDeviation));

		}

	}

}