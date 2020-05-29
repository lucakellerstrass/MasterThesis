package kellerstrass.ModelCalibration.test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import kellerstrass.ModelCalibration.CalibrationMachineInterface;
import kellerstrass.ModelCalibration.HWCalibrationMachine;
import kellerstrass.ModelCalibration.LmmCalibrationMachine;
import kellerstrass.marketInformation.CalibrationInformation;
import kellerstrass.marketInformation.DataScope;
import kellerstrass.marketInformation.DataSource;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.optimizer.SolverException;
import net.finmath.time.TimeDiscretizationFromArray;

public class CalibrationComparism {

	private static DecimalFormat formatterValue = new DecimalFormat(" ##0.000%;-##0.000%",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterParam = new DecimalFormat(" #0.0000;-#0.0000",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation = new DecimalFormat(" 0.00000E00;-0.00000E00",
			new DecimalFormatSymbols(Locale.ENGLISH));

	private static boolean forcedCalculation = false;
	private static int numberOfPaths = 1000;
	//
	private static int numberOfFactorsModel1 = 3; // For Libor Market Model
	private static int numberOfFactorsModel2 = 2; // For Hull white Model

	public static void main(String[] args) throws SolverException, CalculationException {

		// Rising Terminals
		System.out.println("Test of example rising terminals calibration:");
		CalibrationInformation calibrationInformation1 = new CalibrationInformation(DataScope.RisingTerminals,
				DataSource.EXAMPLE);

		System.out.println("We test the Calibraion differences of two models");
		Tester(calibrationInformation1, forcedCalculation);

		// Co-Terminals
		System.out.println("\n" + "Test of example Co-terminals calibration:");
		CalibrationInformation calibrationInformation2 = new CalibrationInformation(DataScope.CoTerminals,
				DataSource.EXAMPLE);

		System.out.println("We test the Calibraion differences of two models");
		Tester(calibrationInformation2, forcedCalculation);

		// Full volatility surface
		System.out.println("\n" + "Test of example Co-terminals calibration:");
		CalibrationInformation calibrationInformation3 = new CalibrationInformation(DataScope.FullSurface,
				DataSource.EXAMPLE);

		System.out.println("We test the Calibraion differences of two models");
		Tester(calibrationInformation3, forcedCalculation);

	}

	private static void Tester(CalibrationInformation calibrationInformation, boolean forcedCalculation)
			throws SolverException, CalculationException {

		// Initialization
		CalibrationMachineInterface model1CalibrationMaschine = new LmmCalibrationMachine(numberOfPaths,
				numberOfFactorsModel1, calibrationInformation);
		// Initialization
		CalibrationMachineInterface model2CalibrationMaschine = new HWCalibrationMachine(numberOfPaths,
				numberOfFactorsModel2, calibrationInformation);

		// The model name
		System.out.println("The name of the Model 1  is: " + model1CalibrationMaschine.getModelName());
		System.out.println("The name of the Model 2  is: " + model2CalibrationMaschine.getModelName());

		// create the process:

		double lastTime = 40.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);
		BrownianMotion brownianMotionModel1 = new net.finmath.montecarlo.BrownianMotionLazyInit(
				timeDiscretizationFromArray, numberOfFactorsModel1, numberOfPaths, 31415 /* seed */);
		BrownianMotion brownianMotionModel2 = new net.finmath.montecarlo.BrownianMotionLazyInit(
				timeDiscretizationFromArray, numberOfFactorsModel2, numberOfPaths, 31415 /* seed */);
		EulerSchemeFromProcessModel process1 = new EulerSchemeFromProcessModel(brownianMotionModel1,
				EulerSchemeFromProcessModel.Scheme.EULER);
		EulerSchemeFromProcessModel process2 = new EulerSchemeFromProcessModel(brownianMotionModel2,
				EulerSchemeFromProcessModel.Scheme.EULER);

		System.out.println("\n" + "Now we test how good the calibration worked for Model 1 and Model 2 and compared.");

		LIBORModelMonteCarloSimulationModel model1SimulationModel = model1CalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(process1, forcedCalculation);
		LIBORModelMonteCarloSimulationModel model2simulationModel = model2CalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(process2, forcedCalculation);

		/* Calibration */

		/*
		 * Create a set of calibration products.
		 */
		// We get the calibration information from the "CalibrationInformation" instance
		CalibrationProduct[] calibrationItems = model1CalibrationMaschine.getCalibrationProducts();

		System.out.println("The calculation of Model 1 took " + model1CalibrationMaschine.getCalculationDuration()
				+ " milli seconds. Which are "
				+ formatterParam.format(model1CalibrationMaschine.getCalculationDuration() / 60000) + " minutes.");
		System.out.println("The calculation of Model 2 took " + model2CalibrationMaschine.getCalculationDuration()
				+ " milli seconds. Which are "
				+ formatterParam.format(model2CalibrationMaschine.getCalculationDuration() / 60000) + " minutes.");

		System.out.println("\nValuation on calibrated model:");
		double deviationSum1 = 0.0;
		double deviationSum2 = 0.0;
		double deviationSquaredSum1 = 0.0;
		double deviationSquaredSum2 = 0.0;
		for (int i = 0; i < calibrationItems.length; i++) {
			AbstractLIBORMonteCarloProduct calibrationProduct = calibrationItems[i].getProduct();
			try {
				double valueModel1 = calibrationProduct.getValue(model1SimulationModel);
				double valueModel2 = calibrationProduct.getValue(model2simulationModel);

				double valueTarget = calibrationItems[i].getTargetValue().getAverage();
				double errorModel1 = valueModel1 - valueTarget;
				double errorModel2 = valueModel2 - valueTarget;

				deviationSum1 += errorModel1;
				deviationSum2 += errorModel2;
				deviationSquaredSum1 += errorModel1 * errorModel1;
				deviationSquaredSum2 += errorModel2 * errorModel2;
				System.out.println(model1CalibrationMaschine.getCalibrationItemNames(calibrationInformation)[i] + "\t"
						+ "Model 1: " + "\t" + formatterValue.format(valueModel1) + "\t" + "Model 2: " + "\t"
						+ formatterValue.format(valueModel2) + "\t Target: " + "\t" + formatterValue.format(valueTarget)
						+ "\t Deviation Model 1: " + "\t" + formatterDeviation.format(valueModel1 - valueTarget)
						+ "\t Deviation Model 2: " + "\t" + formatterDeviation.format(valueModel2 - valueTarget)
						+ "\t Deviation Model 1 to Model 2: " + "\t"
						+ formatterDeviation.format(valueModel1 - valueModel2));

			} catch (Exception e) {
			}
		}
		double averageDeviation1 = deviationSum1 / calibrationItems.length;
		double averageDeviation2 = deviationSum2 / calibrationItems.length;
		System.out.println("Mean Deviation Model 1: \t" + formatterDeviation.format(averageDeviation1));
		System.out.println("RMS Error Model 1 ........: \t"
				+ formatterDeviation.format(Math.sqrt(deviationSquaredSum1 / calibrationItems.length)));
		System.out.println("Mean Deviation Model 2: \t" + formatterDeviation.format(averageDeviation2));
		System.out.println("RMS Error Model 2 ........: \t"
				+ formatterDeviation.format(Math.sqrt(deviationSquaredSum2 / calibrationItems.length)));
		System.out.println(
				"__________________________________________________________________________________________\n");

	}

}