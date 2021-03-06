package kellerstrass.ModelCalibration.test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import kellerstrass.ModelCalibration.CalibrationMachineInterface;
import kellerstrass.ModelCalibration.HWCalibrationMachine;
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

public class HwCalibrationMachineTest2 {

	private static DecimalFormat formatterValue = new DecimalFormat(" ##0.000%;-##0.000%",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterParam = new DecimalFormat(" #0.0000;-#0.0000",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation = new DecimalFormat(" 0.00000E00;-0.00000E00",
			new DecimalFormatSymbols(Locale.ENGLISH));

	private static boolean forcedCalculation = true;
	private static int numberOfPaths = 1000;
	private static int numberOfFactors = 2;

	public static void main(String[] args) throws SolverException, CalculationException {

		System.out.println("Test of example rising terminals calibration:");
		CalibrationInformation calibrationInformation1 = new CalibrationInformation(DataScope.CoTerminals,
				DataSource.EXAMPLE);

		System.out.println("First via the extra test methode of this test class");
		//Tester(calibrationInformation1, forcedCalculation);

		System.out.println("Second via the Calibration maschine intern calibration test methode");
		CalibrationMachineInterface HwCalibrationMaschine = new HWCalibrationMachine(numberOfPaths, numberOfFactors,
				calibrationInformation1);
		//HwCalibrationMaschine.printCalibrationTest(forcedCalculation);
		
		System.out.println("The calibration took " + HwCalibrationMaschine.getCalculationDuration()/60000 + " mins");

		checkForCalibrationItems(calibrationInformation1, forcedCalculation);
		
		
	}

	private static void checkForCalibrationItems(CalibrationInformation calibrationInformation, boolean forcedCalculation)
			throws SolverException, CalculationException {
		
		// Initialization
				CalibrationMachineInterface HwCalibrationMaschine = new HWCalibrationMachine(numberOfPaths, numberOfFactors,
						calibrationInformation);
				
				// create the process:

				double lastTime = 60.0;
				double dt = 0.25;
				TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
						(int) (lastTime / dt), dt);
				BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
						numberOfFactors, numberOfPaths, 31415 /* seed */);
				EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion,
						EulerSchemeFromProcessModel.Scheme.EULER);

				LIBORModelMonteCarloSimulationModel lIBORMarketModelSimulation = HwCalibrationMaschine
						.getLIBORModelMonteCarloSimulationModel(process, forcedCalculation);
				
				// We get the calibration information from the "CalibrationInformation" instance
				CalibrationProduct[] calibrationItems = HwCalibrationMaschine.getCalibrationProducts();
				
				String ItemName = HwCalibrationMaschine.getCalibrationItemNames(calibrationInformation)[0];
				AbstractLIBORMonteCarloProduct calibrationProduct = calibrationItems[0].getProduct();
				
				

				// The model name
				System.out.println("The name of the Model is: " + HwCalibrationMaschine.getModelName());
		
				System.out.println("calibrationProduct 1 is " + ItemName + " and the value is " +  calibrationProduct.getValue(0, lIBORMarketModelSimulation).getAverage());
				System.out.println("the target black implied volatility is "+ calibrationItems[0].getTargetValue().getAverage());
				
				
				
				
				
				
				
		
	}

	
	
	
	
	private static void Tester(CalibrationInformation calibrationInformation, boolean forcedCalculation)
			throws SolverException, CalculationException {

		// Initialization
		CalibrationMachineInterface HwCalibrationMaschine = new HWCalibrationMachine(numberOfPaths, numberOfFactors,
				calibrationInformation);

		// The model name
		System.out.println("The name of the Model is: " + HwCalibrationMaschine.getModelName());

		double[] parameters = HwCalibrationMaschine.getCalibratedParameters();
		System.out.println("The parameters are: ");
		for (int i = 0; i < parameters.length; i++) {
			System.out.println("The Parameter number" + i + " is \t" + formatterParam.format(parameters[i]));
		}

		System.out.println(
				"The calculation took " + HwCalibrationMaschine.getCalculationDuration() + " milli seconds. Which are "
						+ formatterParam.format(HwCalibrationMaschine.getCalculationDuration() / 60000) + " minutes.");

		// create the process:

		double lastTime = 60.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);
		BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				numberOfFactors, numberOfPaths, 31415 /* seed */);
		EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion,
				EulerSchemeFromProcessModel.Scheme.EULER);

		System.out.println("\t" + "Now we test how good the calibration worked");

		LIBORModelMonteCarloSimulationModel lIBORMarketModelSimulation = HwCalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(process, forcedCalculation);

		/* Calibration */

		/*
		 * Create a set of calibration products.
		 */
		// We get the calibration information from the "CalibrationInformation" instance
		CalibrationProduct[] calibrationItems = HwCalibrationMaschine.getCalibrationProducts();

		System.out.println("\nValuation on calibrated model:");
		System.out.println("The number of calibration items is: "+ calibrationItems.length);
		double deviationSum = 0.0;
		double deviationSquaredSum = 0.0;
		for (int i = 0; i < calibrationItems.length; i++) {
			AbstractLIBORMonteCarloProduct calibrationProduct = calibrationItems[i].getProduct();
			try {
				double valueModel = calibrationProduct.getValue(lIBORMarketModelSimulation);
				double valueTarget = calibrationItems[i].getTargetValue().getAverage();
				double error = valueModel - valueTarget;
				deviationSum += error;
				deviationSquaredSum += error * error;
				String[] ItemNames = HwCalibrationMaschine.getCalibrationItemNames(calibrationInformation);
				System.out.println(ItemNames[i] + "\t"
						+ "Model: " + "\t" + formatterValue.format(valueModel) + "\t Target: " + "\t"
						+ formatterValue.format(valueTarget) + "\t Deviation: " + "\t"
						+ formatterDeviation.format(valueModel - valueTarget));
			} catch (Exception e) {
			}
		}
		double averageDeviation = deviationSum / calibrationItems.length;
		System.out.println("Mean Deviation: \t" + formatterDeviation.format(averageDeviation));
		System.out.println("RMS Error.....: \t"
				+ formatterDeviation.format(Math.sqrt(deviationSquaredSum / calibrationItems.length)));
		System.out.println(
				"__________________________________________________________________________________________\n");

	}

}
