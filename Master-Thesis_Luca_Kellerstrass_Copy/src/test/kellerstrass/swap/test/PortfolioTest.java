package kellerstrass.swap.test;

import kellerstrass.ModelCalibration.CalibrationMachineInterface;
import kellerstrass.ModelCalibration.LmmCalibrationMachine;
import kellerstrass.exposure.ExposureMachine;
import kellerstrass.marketInformation.CalibrationInformation;
import kellerstrass.marketInformation.DataScope;
import kellerstrass.marketInformation.DataSource;
import kellerstrass.swap.PortfolioBasic;
import kellerstrass.swap.StoredSwap;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.Swap;
import net.finmath.montecarlo.interestrate.products.TermStructureMonteCarloProduct;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.optimizer.SolverException;
import net.finmath.time.TimeDiscretizationFromArray;

public class PortfolioTest {

	public static void main(String[] args) throws SolverException, CalculationException {

		// Create some swaps
		StoredSwap testStoredSwap1 = new StoredSwap("Example");
		Swap Swap1 = testStoredSwap1.getSwap();
		StoredSwap testStoredSwap2 = new StoredSwap("Example 2");
		Swap Swap2 = testStoredSwap2.getSwap();
		AbstractLIBORMonteCarloProduct[] swaps = { Swap1, Swap2 };

		// Create the portfolio

		AbstractLIBORMonteCarloProduct portfolio = new PortfolioBasic(swaps);

		System.out.println(portfolio);

		// create a model to see how the portfolio behaves
		boolean forcedCalculation = false;

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
		CalibrationInformation calibrationInformation = new CalibrationInformation(DataScope.FullSurface,
				DataSource.EXAMPLE);
		CalibrationMachineInterface lmmCalibrationMaschine = new LmmCalibrationMachine(numberOfPaths, numberOfFactors,
				calibrationInformation);
		// simulation machine
		LIBORModelMonteCarloSimulationModel simulationModel = lmmCalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(process, forcedCalculation);
		System.out.println("The name of the Model is: " + lmmCalibrationMaschine.getModelName());

		// create the exposure estimators
		TermStructureMonteCarloProduct swapExposureEstimator = new ExposureMachine(Swap1);
		TermStructureMonteCarloProduct PortfolioExposureEstimator = new ExposureMachine(portfolio);

		// print the exposures of the single swap and the portfolio
		System.out.println("\n The expected exposure of the first swap:");
		ExposureMachine.printExpectedExposurePaths(swapExposureEstimator, simulationModel, Swap1);

		System.out.println("\n The expected exposure of the second swap:");
		ExposureMachine.printExpectedExposurePaths(PortfolioExposureEstimator, simulationModel, Swap2);

		System.out.println("\n The expected exposure of the Portfolio consisting of two swaps");
		ExposureMachine.printExpectedExposurePaths(PortfolioExposureEstimator, simulationModel, portfolio);

	}

}
