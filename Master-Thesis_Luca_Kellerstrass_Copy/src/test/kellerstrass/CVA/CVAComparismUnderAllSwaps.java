package kellerstrass.CVA;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import kellerstrass.ModelCalibration.CalibrationMachineInterface;
import kellerstrass.ModelCalibration.CurveModelCalibrationMachine;
import kellerstrass.ModelCalibration.HWCalibrationMachine;
import kellerstrass.ModelCalibration.LmmCalibrationMachine;
import kellerstrass.PythonComunication.CVAandCalibrationTestForPython;
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

public class CVAComparismUnderAllSwaps {

	private final static NumberFormat formatter6 = new DecimalFormat("0.000000",
			new DecimalFormatSymbols(new Locale("en")));
	private static DecimalFormat formatterValue = new DecimalFormat(" ##0.00000;-##0.00000",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation = new DecimalFormat(" 0.00000E00;-0.00000E00",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterPercentage = new DecimalFormat(" ##0.000%;-##0.000%",
			new DecimalFormatSymbols(Locale.ENGLISH));

	public static void main(String[] args) throws SolverException, CalculationException  {
		boolean forcedCalculation = false;

		

		// Set the Calibration basket
		DataSource dataSource = DataSource.Market24_10_2019; // relevant for both models

		CalibrationInformation calibrationInformationLMM = new CalibrationInformation(DataScope.FullSurface,
				dataSource);
		CalibrationInformation calibrationInformationHW = new CalibrationInformation(DataScope.CoTerminals, dataSource);

		CurveModelCalibrationMachine curveModelCalibrationMaschine = new CurveModelCalibrationMachine(
				CurveModelDataType.OIS6M2410);

		String referenceDate = "24.10.2019";
		int numberOfPaths = 1000;
		int numberOfFactorsLMM = 3; // For Libor Market Model

		// Simulation time discretization
		double lastTime = 40.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);

		// brownian motion
		BrownianMotion brownianMotionLMM = new net.finmath.montecarlo.BrownianMotionLazyInit(
				timeDiscretizationFromArray, numberOfFactorsLMM, numberOfPaths, 31415 /* seed */);
		BrownianMotion brownianMotionHW = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				2 /*numberOfFactors*/, numberOfPaths, 31415 /* seed */);

		// process
		EulerSchemeFromProcessModel processLMM = new EulerSchemeFromProcessModel(brownianMotionLMM,
				EulerSchemeFromProcessModel.Scheme.EULER);
		EulerSchemeFromProcessModel processHW = new EulerSchemeFromProcessModel(brownianMotionHW,
				EulerSchemeFromProcessModel.Scheme.EULER);

		// calibration machine
		CalibrationMachineInterface LMMCalibrationMaschine = new LmmCalibrationMachine(numberOfPaths, numberOfFactorsLMM,
				calibrationInformationLMM, curveModelCalibrationMaschine);
		CalibrationMachineInterface HWCalibrationMaschine = new HWCalibrationMachine(numberOfPaths, 2 /*numberOfFactors*/,
				calibrationInformationHW, curveModelCalibrationMaschine);

		// simulation models
		LIBORModelMonteCarloSimulationModel LMsimulationModel = LMMCalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(processLMM, forcedCalculation);
		LIBORModelMonteCarloSimulationModel HWsimulationModel = HWCalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(processHW, forcedCalculation);

			
				
		System.out.println("We want to compare the given models");
		System.out.println("LIBOR Market Model is: \t" + LMMCalibrationMaschine.getModelName());
		System.out.println("Hull White Model is: \t" + HWCalibrationMaschine.getModelName() + "\n");
				
				
				
				
				
				
		// Swap
		// initiate the swaps that are capture by the volatility matrix for calibration.

		// Basics for the swap
		String BuySell = "Buy";
		int notional = 1000000;
		double fixedRate = -1.0; // We use them ATM // For a rate take 0.00547
		String fixedFrequency = "1Y";
		String floatFrequency = "6M";
		String RateFrequency = "6M";
		String fixedCouponConvention = "ACT/365";
		String xiborCouponConvention = "ACT/365";

		// Counter party information
		double recoveryRate = 0.4;
		double[] cdsSpreads = { 6, 9, 15, 22, 35, 40, 45, 45.67, 46.33, 47 }; // { 300.0, 350.0, 400.0, 450.0, 500.0,
																				// 550.0, 600.0, 650.0, 700.0, 750.0 };

		String[] experiesFromLMM = LMMCalibrationMaschine.getCalibrationItemExpiries(calibrationInformationLMM);
		String[] tenorsFromLMM = LMMCalibrationMaschine.getCalibrationItemTenors(calibrationInformationLMM);

		
		double[] CVADifferenceRelativeToLMM = new double[experiesFromLMM.length];
		double[] CVADifferenceRelativeToNominal = new double[experiesFromLMM.length];
		
		System.out.println("experies" + "   \t   " + "tenors" + "   \t   " + "cvaLMValue" + "   \t   " + "cvaHWValue" + "   \t   " + "CVADifferenceRelativeToLMM"+ "   \t   " + "CVADifferenceRelativeToNominal");
		
		for (int i = 0; i < experiesFromLMM.length; i++) {
			

			// create the corresponding swaps
			String swapStart = experiesFromLMM[i];
			String swapTenor = tenorsFromLMM[i];

			StoredSwap swapInitialization = new StoredSwap(swapStart + " " + swapTenor, BuySell, notional, fixedRate, referenceDate,
					swapStart, swapTenor, fixedFrequency, floatFrequency, RateFrequency, fixedCouponConvention,
					xiborCouponConvention);
				swapInitialization.changeToATMswap(LMMCalibrationMaschine.getForwardCurve(), LMMCalibrationMaschine.getCurveModel());
			Swap swap = swapInitialization.getSwap();
			

			
			
			CVA cvaLM = new CVA(LMsimulationModel, swap, recoveryRate, cdsSpreads, LMMCalibrationMaschine.getDiscountCurve());
			CVA cvaHW = new CVA(HWsimulationModel, swap, recoveryRate, cdsSpreads, HWCalibrationMaschine.getDiscountCurve());
			double cvaLMValue;
			double cvaHWValue;
			try {
				cvaLMValue = cvaLM.getValue();
				cvaHWValue = cvaHW.getValue();
			} catch (Exception e) {
				continue;
			}			
			
			
			System.out.print(experiesFromLMM[i] + "\t" + tenorsFromLMM[i] + "\t");
			System.out.print(cvaLMValue + "\t" + cvaHWValue + "\t");
			
			
			CVADifferenceRelativeToLMM[i] = (cvaLMValue - cvaHWValue)/cvaLMValue;
			CVADifferenceRelativeToNominal[i] = (cvaLMValue - cvaHWValue)/notional;
			
			System.out.println(CVADifferenceRelativeToLMM[i] + "\t" + CVADifferenceRelativeToNominal[i]);
		}



	}

}
