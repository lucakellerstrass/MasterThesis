package kellerstrass.LMMCalibration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kellerstrass.CVA.CVA;
import kellerstrass.ModelCalibration.CalibrationMachineInterface;
import kellerstrass.ModelCalibration.CurveModelCalibrationMachine;
import kellerstrass.ModelCalibration.HWCalibrationMachine;
import kellerstrass.ModelCalibration.LmmCalibrationMachine;
import kellerstrass.marketInformation.CalibrationInformation;
import kellerstrass.marketInformation.CurveModelDataType;
import kellerstrass.marketInformation.DataScope;
import kellerstrass.marketInformation.DataSource;
import kellerstrass.swap.PortfolioExtended;
import kellerstrass.swap.StoredSwap;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.Swap;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.montecarlo.process.MonteCarloProcess;
import net.finmath.time.TimeDiscretizationFromArray;

public class CVAValuationOnCalSurface {

	public static void main(String[] args) throws Exception {
		boolean forcedCalculation = false;

		// Basic information
		String referenceDate = "23.10.2019";
		int numberOfPaths = 1000;
		int numberOfFactorsLMM = 3; // For Libor Market Model

		// Simulation time discretization
		double lastTime = 40.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);

		// calibration information
		DataSource dataSource = DataSource.Market23_10_2019; // relevant for both models
		CalibrationInformation calibrationInformationLMM = new CalibrationInformation(DataScope.MonstlyCoTerminals,
				dataSource);
		CalibrationInformation calibrationInformationHW = new CalibrationInformation(DataScope.FullSurface, dataSource);
		CurveModelCalibrationMachine curveModelCalibrationMaschine = new CurveModelCalibrationMachine(
				CurveModelDataType.OIS6M2310);

		// calibration machines
		CalibrationMachineInterface LMMCalibrationMaschine = new LmmCalibrationMachine(numberOfPaths,
				numberOfFactorsLMM, calibrationInformationLMM, curveModelCalibrationMaschine);
		CalibrationMachineInterface HWCalibrationMaschine = new HWCalibrationMachine(numberOfPaths,
				2 /* numberOfFactors */, calibrationInformationHW, curveModelCalibrationMaschine);

		// simulation models
		LIBORModelMonteCarloSimulationModel LMsimulationModel = LMMCalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(getProcess(numberOfFactorsLMM, numberOfPaths),
						forcedCalculation);
		LIBORModelMonteCarloSimulationModel HWsimulationModel = HWCalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(getProcess(2, numberOfPaths), forcedCalculation);



		// Counter party information
				double recoveryRate = 0.4;
				double[] cdsSpreads = { 6, 9, 15, 22, 35, 40, 45, 45.67, 46.33, 47 };
//		CVA cvaLM = new CVA(LMsimulationModel, coTerminalPortfolio, recoveryRate, cdsSpreads, LMMCalibrationMaschine.getDiscountCurve());
//		CVA cvaHW = new CVA(HWsimulationModel, coTerminalPortfolio, recoveryRate, cdsSpreads, HWCalibrationMaschine.getDiscountCurve());
		
		
//		System.out.println("CVA of LMM= "+cvaLM.getValue() + "    and CVA for HW = "+ cvaHW.getValue() );
		
		
	}

	/**
	 * Returns a discretization process for given factors of the Brownian motion and
	 * the number of Paths
	 * 
	 * @param numberOfFactors
	 * @param numberOfPaths
	 * @return
	 */
	private static MonteCarloProcess getProcess(int numberOfFactors, int numberOfPaths) {
		// Simulation time discretization
		double lastTime = 40.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);
		BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				numberOfFactors, numberOfPaths, 31415 /* seed */);
		return new EulerSchemeFromProcessModel(brownianMotion, EulerSchemeFromProcessModel.Scheme.EULER);
	}


}
