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

public class CVAValuationOnTheCoTerminalPortfolio {

	public static void main(String[] args) throws Exception {
		boolean forcedCalculation = false;

		// Basic information
		String referenceDate = "24.10.2019";
		int numberOfPaths = 1000;
		int numberOfFactorsLMM = 3; // For Libor Market Model

		// Simulation time discretization
		double lastTime = 40.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);

		// calibration information
		DataSource dataSource = DataSource.Market24_10_2019; // relevant for both models
		CalibrationInformation calibrationInformationLMM = new CalibrationInformation(DataScope.FullSurface,
				dataSource);
		CalibrationInformation calibrationInformationHW = new CalibrationInformation(DataScope.FullSurface, dataSource);
		CurveModelCalibrationMachine curveModelCalibrationMaschine = new CurveModelCalibrationMachine(
				CurveModelDataType.OIS6M2410);

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

		PortfolioExtended coTerminalPortfolio = createPortfolioOfATMSwaptionCoTerminals(
				LMMCalibrationMaschine.getForwardCurve(), LMMCalibrationMaschine.getCurveModel(), referenceDate);

		// Counter party information
				double recoveryRate = 0.4;
				double[] cdsSpreads = { 6, 9, 15, 22, 35, 40, 45, 45.67, 46.33, 47 };
		CVA cvaLM = new CVA(LMsimulationModel, coTerminalPortfolio, recoveryRate, cdsSpreads, LMMCalibrationMaschine.getDiscountCurve());
		CVA cvaHW = new CVA(HWsimulationModel, coTerminalPortfolio, recoveryRate, cdsSpreads, HWCalibrationMaschine.getDiscountCurve());
		
		double cvaValueLMM = cvaLM.getValue();
		double cvaValueHW = cvaHW.getValue();
		
		System.out.println("CVA of LMM= \t"+cvaValueLMM + "\t and CVA for HW = \t"+ cvaValueHW+ "\t deviation = \t"+(cvaValueLMM -cvaValueHW)/cvaValueLMM);
		
		
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

	/**
	 * Create a Portfolio Of ATM Swaption 11Y CoTerminals
	 * 
	 * @param forwardCruve
	 * @param curveModel
	 * @param referenceDate
	 * @return
	 */
	private static PortfolioExtended createPortfolioOfATMSwaptionCoTerminals(ForwardCurve forwardCruve,
			AnalyticModel curveModel, String referenceDate) {
		// Basics for the swap
		String BuySell = "Buy";
		int notional = 1000000;
		// We use them ATM // double fixedRate = 0.00547;
		String fixedFrequency = "1Y";
		String floatFrequency = "6M";
		String RateFrequency = "6M";
		String fixedCouponConvention = "ACT/365";
		String xiborCouponConvention = "ACT/365";

		// The Co-Terminals
		String[] ExpiriesCoTerminals = { "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y" };
		String[] tenorsCoTerminals = { "10Y", "9Y", "8Y", "7Y", "6Y", "4Y", "1Y" };
		// weight = 1/tenor
		double[] weightsCoterminals = { 1.0 / 10.0, 1.0 / 9.0, 1.0 / 8.0, 1.0 / 7.0, 1.0 / 6.0, 1.0 / 4.0, 1.0 / 1.0 };

		
		List<AbstractLIBORMonteCarloProduct> product = new ArrayList<AbstractLIBORMonteCarloProduct>();	
		
		List<Double> weights = new ArrayList<Double>();

		for (int i = 0; i < ExpiriesCoTerminals.length; i++) {

			// create the corresponding swaps
			String swapStart = ExpiriesCoTerminals[i];
			String swapTenor = tenorsCoTerminals[i];
			StoredSwap swapInitialization = new StoredSwap(swapStart + " " + swapTenor, BuySell, notional,
					0.0 /* fixed rate */, referenceDate, swapStart, swapTenor, fixedFrequency, floatFrequency,
					RateFrequency, fixedCouponConvention, xiborCouponConvention);
			swapInitialization.changeToATMswap(forwardCruve, curveModel);

			Swap swap = swapInitialization.getSwap();
			
			
			

			product.add(swap);
			weights.add(weightsCoterminals[i]);

		}
		return new PortfolioExtended(product, weights);
	}

}
