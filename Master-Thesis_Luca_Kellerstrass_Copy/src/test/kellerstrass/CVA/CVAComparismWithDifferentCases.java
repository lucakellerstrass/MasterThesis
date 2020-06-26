package kellerstrass.CVA;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

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

public class CVAComparismWithDifferentCases {

	private final static NumberFormat formatter6 = new DecimalFormat("0.000000",
			new DecimalFormatSymbols(new Locale("en")));
	private static DecimalFormat formatterValue = new DecimalFormat(" ##0.00000;-##0.00000",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation = new DecimalFormat(" 0.00000E00;-0.00000E00",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterPercentage = new DecimalFormat(" ##0.000%;-##0.000%",
			new DecimalFormatSymbols(Locale.ENGLISH));

	public static void main(String[] args) throws Exception {
		boolean forcedCalculation = false;

		
		
		
		// Set the Calibration set. Here: e.g. Example Co-Terminals
		CalibrationInformation calibrationInformationLMM = new CalibrationInformation(DataScope.FullSurface,
				DataSource.Market23_10_2019);
		
		CalibrationInformation calibrationInformationHW = new CalibrationInformation(DataScope.CoTerminals,
				DataSource.Market23_10_2019);

		CurveModelCalibrationMachine curveModelCalibrationMaschine = new CurveModelCalibrationMachine(
				CurveModelDataType.OIS6M2310);

		int numberOfPaths = 1000;
		int numberOfFactorsM1 = 3; // For Libor Market Model
		int numberOfFactorsM2 = 2; // For Hull white Model

		// Simulation time discretization
		double lastTime = 40.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);

		// brownian motion
		BrownianMotion brownianMotionLMM = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				numberOfFactorsM1, numberOfPaths, 31415 /* seed */);
		BrownianMotion brownianMotionHW = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				numberOfFactorsM2, numberOfPaths, 31415 /* seed */);
		
		// process
		EulerSchemeFromProcessModel processLMM = new EulerSchemeFromProcessModel(brownianMotionLMM,
				EulerSchemeFromProcessModel.Scheme.EULER);
		EulerSchemeFromProcessModel processHW = new EulerSchemeFromProcessModel(brownianMotionHW,
				EulerSchemeFromProcessModel.Scheme.EULER);
		// calibration machine
		
		
		CalibrationMachineInterface LMMCalibrationMaschine = new LmmCalibrationMachine(numberOfPaths,
				numberOfFactorsM1, calibrationInformationLMM, curveModelCalibrationMaschine);
		CalibrationMachineInterface HWCalibrationMaschine = new HWCalibrationMachine(numberOfPaths,
				numberOfFactorsM2, calibrationInformationHW, curveModelCalibrationMaschine);
		
		
		
		
		// simulation machine
		LIBORModelMonteCarloSimulationModel LMsimulationModel = LMMCalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(processLMM, forcedCalculation);
		
				
		
		LIBORModelMonteCarloSimulationModel HWsimulationModel = HWCalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(processHW, forcedCalculation);

		// Swap
		StoredSwap testStoredSwap = new StoredSwap("Example 2");
		
		
		testStoredSwap.changeToATMswap(LMMCalibrationMaschine.getForwardCurve(), LMMCalibrationMaschine.getCurveModel());
		
		Swap testSwap = testStoredSwap.getSwap();

		double recoveryRate = 0.4;

		double[] cdsSpreads =  {6, 9, 15, 22, 35, 40, 45, 45.67, 46.33, 47 };   //{ 300.0, 350.0, 400.0, 450.0, 500.0, 550.0, 600.0, 650.0, 700.0, 750.0 };

		CVA cvaLM = new CVA(LMsimulationModel, testSwap, recoveryRate, cdsSpreads, LMMCalibrationMaschine.getDiscountCurve());
		CVA cvaHW = new CVA(HWsimulationModel, testSwap, recoveryRate, cdsSpreads, HWCalibrationMaschine.getDiscountCurve());
		double cvaValueM1 = cvaLM.getValue();
		double cvaValueM2 = cvaHW.getValue();

		// Exposure Maschine
		// ExposureMaschine exposureMaschine = new ExposureMaschine(testSwap);
		TermStructureMonteCarloProduct swapExposureEstimator = new ExposureMachine(testSwap);

		System.out.println("\n We want to compare the given models");
		System.out.println("Model 1 is: " + LMMCalibrationMaschine.getModelName());
		System.out.println("Model 2 is: " + HWCalibrationMaschine.getModelName() + "\n");

		System.out.println("The CVA with Model 1 is \t" + formatterValue.format(cvaValueM1));
		System.out.println("The CVA with Model 2 is \t" + formatterValue.format(cvaValueM2));
		System.out.println("The deviation (CVA1 - CVA2) is: \t" + formatterValue.format(cvaValueM1 - cvaValueM2)
				+ ", which is " + formatterPercentage.format((cvaValueM1 - cvaValueM2) / cvaValueM1) + "\n");

		// Print the Calibration Tests for two two models
		LMMCalibrationMaschine.printCalibrationTest();
		HWCalibrationMaschine.printCalibrationTest();

		printExpectedExposurePathsCpmarism(swapExposureEstimator, LMsimulationModel, HWsimulationModel, testSwap);

	}

	private static void printExpectedExposurePathsCpmarism(TermStructureMonteCarloProduct swapExposureEstimator,
			LIBORModelMonteCarloSimulationModel Model1, LIBORModelMonteCarloSimulationModel Model2,
			AbstractLIBORMonteCarloProduct testSwap) throws CalculationException {
		System.out.println(
				"observationDate  \t Model 1:   \t   expected Exposure   \t   expected positive Exposure   \t   expected negative Exposure "
						+ "\t Model 2:   \t   expected Exposure   \t   expected positive Exposure   \t   expected negative Exposure"
						+ "\t deviation Model 1 - Model 2: \t   expected positive Exposure   \t   expected negative Exposure");
		for (double observationDate : Model1.getTimeDiscretization()) {

			/*
			 * if(observationDate == 0) { continue; }
			 */

			/*
			 * Calculate expected positive exposure of a swap
			 */
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
					+ formatter6.format(valuesEstimatedExposureM1.getAverage()) + "    \t                        "
					+ formatter6.format(expectedPositiveExposureM1) + "    \t                        "
					+ formatter6.format(expectedNegativeExposureM1) + "\t                                   \t"
					+ formatter6.format(valuesEstimatedExposureM2.getAverage()) + "    \t                        "
					+ formatter6.format(expectedPositiveExposureM2) + "    \t                        "
					+ formatter6.format(expectedNegativeExposureM2) + "\t                                   \t"
					+ formatter6.format(expectedPositiveExposureDeviation) + "    \t                        "
					+ formatter6.format(expectedNegativeExposureDeviation));

		}

	}

}