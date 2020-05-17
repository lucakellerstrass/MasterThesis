package kellerstrass.exposure.test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import kellerstrass.ModelCalibration.CalibrationMaschineInterface;
import kellerstrass.ModelCalibration.HWCalibrationMaschine;
import kellerstrass.ModelCalibration.LmmCalibrationMaschine;
import kellerstrass.exposure.ExposureMaschine;
import kellerstrass.marketInformation.CalibrationInformation;
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

public class ExposureComparism {

	private final static NumberFormat formatter6 = new DecimalFormat("0.000000", new DecimalFormatSymbols(new Locale("en")));
	private static DecimalFormat formatterDeviation	= new DecimalFormat(" 0.00000E00;-0.00000E00", new DecimalFormatSymbols(Locale.ENGLISH));
	
	// Set the Calibration set. Here: e.g. Example Co-Terminals
		private static	CalibrationInformation calibrationInformation = new CalibrationInformation(DataScope.FullSurface, DataSource.EXAMPLE );
	
	
	public static void main(String[] args) throws CalculationException, SolverException {
boolean forcedCalculation = false;
		
		int numberOfPaths = 1000;
		int numberOfFactors = 3;
		
		//Simulation time discretization
		double lastTime = 40.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,(int) (lastTime / dt), dt);
		//brownian motion
		BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray, numberOfFactors , numberOfPaths, 31415 /* seed */);
		//process
		EulerSchemeFromProcessModel process1 = new EulerSchemeFromProcessModel(brownianMotion, EulerSchemeFromProcessModel.Scheme.EULER);
		EulerSchemeFromProcessModel process2 = new EulerSchemeFromProcessModel(brownianMotion, EulerSchemeFromProcessModel.Scheme.EULER);
		//calibration machine
		CalibrationMaschineInterface Model1CalibrationMaschine = new LmmCalibrationMaschine(numberOfPaths, numberOfFactors, calibrationInformation);
		CalibrationMaschineInterface Model2CalibrationMaschine = new HWCalibrationMaschine(numberOfPaths, 2, calibrationInformation);
	    //simulation machine
		LIBORModelMonteCarloSimulationModel Model1 = Model1CalibrationMaschine.getLIBORModelMonteCarloSimulationModel(process1,forcedCalculation);
		LIBORModelMonteCarloSimulationModel Model2 = Model2CalibrationMaschine.getLIBORModelMonteCarloSimulationModel(process2,forcedCalculation);

		//Swap
		StoredSwap testStoredSwap = new StoredSwap("Example");
		Swap testSwap = testStoredSwap.getSwap();
		
		
		//Exposure Maschine
		//ExposureMaschine exposureMaschine = new ExposureMaschine(testSwap);
		TermStructureMonteCarloProduct swapExposureEstimator = new ExposureMaschine(testSwap);
		
		
		 System.out.println("\n We want to compare the exposure paths of the given model an the swap: ");
		
		 
		 System.out.println("Model 1 is: "+ Model1CalibrationMaschine.getModelName());
		 System.out.println("Model 2 is: "+ Model2CalibrationMaschine.getModelName());
		 printExpectedExposurePathsCpmarism(swapExposureEstimator, Model1, Model2, testSwap);


	}
	
	
	private static void printExpectedExposurePathsCpmarism(TermStructureMonteCarloProduct swapExposureEstimator,
			LIBORModelMonteCarloSimulationModel Model1, LIBORModelMonteCarloSimulationModel Model2, AbstractLIBORMonteCarloProduct testSwap) throws CalculationException {
		System.out.println("observationDate  \t Model 1: \t   expected positive Exposure   \t   expected negative Exposure "
				+ "\t Model 2: \t   expected positive Exposure   \t   expected negative Exposure"
				+ "\t Deviation Model 1 - Model 2: \t   expected positive Exposure   \t   expected negative Exposure");
		for(double observationDate : Model1.getTimeDiscretization()) {

			/*if(observationDate == 0) {
				continue;
			}*/

			/*
			 * Calculate expected positive exposure of a swap
			 */
			//Model 1
			RandomVariable valuesSwapM1 = testSwap.getValue(observationDate, Model1);
			RandomVariable valuesEstimatedExposureM1 = swapExposureEstimator.getValue(observationDate, Model1);
			RandomVariable valuesPositiveExposureM1 = valuesSwapM1.mult(valuesEstimatedExposureM1.choose(new RandomVariableFromDoubleArray(1.0), new RandomVariableFromDoubleArray(0.0)));
			RandomVariable valuesNegativeExposureM1 = valuesSwapM1.mult(valuesEstimatedExposureM1.choose(new RandomVariableFromDoubleArray(0.0), new RandomVariableFromDoubleArray(1.0)));			
			double expectedPositiveExposureM1		= valuesPositiveExposureM1.getAverage();
			double expectedNegativeExposureM1		= valuesNegativeExposureM1.getAverage();
			
			//Model 2
			RandomVariable valuesSwapM2= testSwap.getValue(observationDate, Model2);
			RandomVariable valuesEstimatedExposureM2 = swapExposureEstimator.getValue(observationDate, Model2);
			RandomVariable valuesPositiveExposureM2 = valuesSwapM2.mult(valuesEstimatedExposureM2.choose(new RandomVariableFromDoubleArray(1.0), new RandomVariableFromDoubleArray(0.0)));
			RandomVariable valuesNegativeExposureM2 = valuesSwapM2.mult(valuesEstimatedExposureM2.choose(new RandomVariableFromDoubleArray(0.0), new RandomVariableFromDoubleArray(1.0)));			
			double expectedPositiveExposureM2		= valuesPositiveExposureM2.getAverage();
			double expectedNegativeExposureM2		= valuesNegativeExposureM2.getAverage();
			
			double expectedPositiveExposureDeviation		= expectedPositiveExposureM1 - expectedPositiveExposureM2;
			double expectedNegativeExposureDeviation		= expectedNegativeExposureM1 - expectedNegativeExposureM2;
			

			System.out.println(observationDate + "    \t                                   \t" +  formatter6.format(expectedPositiveExposureM1) + "    \t                        " +  formatter6.format(expectedNegativeExposureM1)
			+ "\t                                   \t" +  formatter6.format(expectedPositiveExposureM2) + "    \t                        " +  formatter6.format(expectedNegativeExposureM2)
			+ "\t                                   \t" +  formatter6.format(expectedPositiveExposureDeviation) + "    \t                        " +  formatter6.format(expectedNegativeExposureDeviation)		);


		}
	
	}

}
