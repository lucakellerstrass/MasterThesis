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
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * Test the CVA an exposure with some "real" data input.
 * 
 * @author lucak
 *
 */
public class CVATestWithMarketData {


	private final static NumberFormat formatter6 = new DecimalFormat("0.000000",
			new DecimalFormatSymbols(new Locale("en")));
	private static DecimalFormat formatterValue = new DecimalFormat(" ##0.00000;-##0.00000",
			new DecimalFormatSymbols(Locale.ENGLISH));

	public static void main(String[] args) throws Exception {
		boolean forcedCalculation = false;
		
		// Set the Calibration set. Here: e.g. Example Co-Terminals
		CalibrationInformation calibrationInformation = new CalibrationInformation(DataScope.FullSurface,
				DataSource.EXAMPLE);//EXAMPLE
		CurveModelCalibrationMachine curveModelCalibrationMaschine = new CurveModelCalibrationMachine(
				CurveModelDataType.Example);
		

		int numberOfPaths = 1000;
		int numberOfFactors = 3;

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
		CalibrationMachineInterface lmmCalibrationMaschine = new LmmCalibrationMachine(numberOfPaths, numberOfFactors,
				calibrationInformation, curveModelCalibrationMaschine);
		// simulation machine
		LIBORModelMonteCarloSimulationModel simulationModel = lmmCalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(process, forcedCalculation);

		// Swap
		StoredSwap StoredTrueSwap = new StoredSwap("TrueSwap1");
		// StoredTrueSwap.setFixedCoupon(-0.003492800749460123);
		System.out.println("fixed = " + StoredTrueSwap.getFixedCoupon());
		Swap TrueSwap = StoredTrueSwap.getSwap();

		// AbstractLIBORMonteCarloProduct testSwap =
		// LMMCalibrationCVA5RebuildAsCalibrationmaschine.getSwap();

		// Exposure Maschine
		// ExposureMaschine exposureMaschine = new ExposureMaschine(testSwap);
		ExposureMachine swapExposureEstimator = new ExposureMachine(TrueSwap);

		System.out.println("The name of the Model is: " + lmmCalibrationMaschine.getModelName());

		System.out.println("\n We whant to to the exposure paths of the given model an the swap: " /*
																									 * + testStoredSwap.
																									 * getSwapName()
																									 */);

		printExpectedExposurePaths(swapExposureEstimator, simulationModel);

		double recoveryRate = 0.4;

		double[] cdsSpreads = { 6, 9, 15, 22, 35, 40, 45, 45.67, 46.33, 47 };

		CVA cva = new CVA(simulationModel, TrueSwap, recoveryRate, cdsSpreads,
				lmmCalibrationMaschine.getDiscountCurve());

		System.out.println("The CVA is \t" + formatterValue.format(cva.getValue()));

		System.out.println("Calibration test:");
		lmmCalibrationMaschine.printCalibrationTest();

	}

	private static void printExpectedExposurePaths(ExposureMachine swapExposureEstimator,
			LIBORModelMonteCarloSimulationModel simulationModel) throws CalculationException {

		AbstractLIBORMonteCarloProduct Swap = swapExposureEstimator.getUnterlying();

		System.out.println(
				"observationDate"
				//+ "  \t forward rate"
				+ "   \t   expected positive Exposure  \t   expected negative Exposure");
		int timeIndex;
		int liborPeriodIndex;

		for (double observationDate : simulationModel.getTimeDiscretization()) {
			timeIndex = simulationModel.getTimeDiscretization().getTimeIndex(Math.min(observationDate, 39.0));
			liborPeriodIndex = simulationModel.getLiborPeriodIndex(Math.min(observationDate, 39.0));
			/*
			 * if(observationDate == 0) { continue; }
			 */

			/*
			 * Calculate expected positive exposure of a swap
			 */

			RandomVariable valuesSwap = Swap.getValue(observationDate, simulationModel);
			RandomVariable valuesEstimatedExposure = swapExposureEstimator.getValue(observationDate, simulationModel);
			RandomVariable valuesPositiveExposure = valuesSwap.mult(valuesEstimatedExposure
					.choose(new RandomVariableFromDoubleArray(1.0), new RandomVariableFromDoubleArray(0.0)));
			RandomVariable valuesNegativeExposure = valuesSwap.mult(valuesEstimatedExposure
					.choose(new RandomVariableFromDoubleArray(0.0), new RandomVariableFromDoubleArray(1.0)));

			double expectedPositiveExposure = valuesPositiveExposure.getAverage();
			double expectedNegativeExposure = valuesNegativeExposure.getAverage();

			System.out.println(observationDate + "    \t         "
					//+ simulationModel.getLIBOR(timeIndex, liborPeriodIndex).getAverage() + "    \t         "
					+ formatter6.format(expectedPositiveExposure) + "    \t         "
					+ formatter6.format(expectedNegativeExposure));

		}
	}

}
