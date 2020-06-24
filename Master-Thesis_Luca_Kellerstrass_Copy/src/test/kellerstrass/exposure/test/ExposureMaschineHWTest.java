package kellerstrass.exposure.test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import kellerstrass.ModelCalibration.CalibrationMachineInterface;
import kellerstrass.ModelCalibration.HWCalibrationMachine;
import kellerstrass.ModelCalibration.HWCalibrationMachineAlternative;
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
import net.finmath.optimizer.SolverException;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretizationFromArray;

public class ExposureMaschineHWTest {

	private static DecimalFormat formatterValue = new DecimalFormat(" ##0.000%;-##0.000%",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterParam = new DecimalFormat(" #0.000;-#0.000",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation = new DecimalFormat(" 0.00000E00;-0.00000E00",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private final static NumberFormat formatter6 = new DecimalFormat("0.000000",
			new DecimalFormatSymbols(new Locale("en")));

	

	public static void main(String[] args) throws SolverException, CalculationException {
		
		// Set the Calibration set. Here: e.g. Example Co-Terminals
		CalibrationInformation calibrationInformation = new CalibrationInformation(DataScope.FullSurface,
				DataSource.Market24_10_2019);
		
		CurveModelDataType curveModelDataType = CurveModelDataType.OIS6M2410;

		boolean forcedCalculation = false;

		int numberOfPaths = 1000;
		int numberOfFactors = 2;

		// Simulation time discretization
		double lastTime = 40.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);
		// brownian motion
		BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				numberOfFactors, numberOfPaths, 31415 /* seed */);
		// process
		EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion,
				EulerSchemeFromProcessModel.Scheme.EULER);
		// calibration machine
		CalibrationMachineInterface HwCalibrationMaschine = new HWCalibrationMachine(numberOfPaths, numberOfFactors,
				calibrationInformation, curveModelDataType);
		// simulation machine
		LIBORModelMonteCarloSimulationModel simulationModel = HwCalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(process, forcedCalculation);

		// Swap
		StoredSwap testStoredSwap = new StoredSwap("Example 2");
		testStoredSwap.changeToATMswap(HwCalibrationMaschine.getForwardCurve(), HwCalibrationMaschine.getCurveModel());
		Swap testSwap = testStoredSwap.getSwap();

		// AbstractLIBORMonteCarloProduct testSwap =
		// LMMCalibrationCVA5RebuildAsCalibrationmaschine.getSwap();

		// Exposure Maschine
		// ExposureMaschine exposureMaschine = new ExposureMaschine(testSwap);
		TermStructureMonteCarloProduct swapExposureEstimator = new ExposureMachine(testSwap);

		System.out.println("The name of the Model is: " + HwCalibrationMaschine.getModelName());

		System.out.println("\n We whant to to the exposure paths of the given model an the swap: " /*
																									 * + testStoredSwap.
																									 * getSwapName()
																									 */);

		printExpectedExposurePaths(swapExposureEstimator, simulationModel, testSwap);

	}

	private static void printExpectedExposurePaths(TermStructureMonteCarloProduct swapExposureEstimator,
			LIBORModelMonteCarloSimulationModel simulationModel, AbstractLIBORMonteCarloProduct testSwap)
			throws CalculationException {
		System.out.println("observationDate  \t   expected positive Exposure  \t   expected negative Exposure");
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

		}

	}

}
