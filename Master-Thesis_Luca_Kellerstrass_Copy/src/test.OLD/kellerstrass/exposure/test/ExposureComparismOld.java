package kellerstrass.exposure.test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

import org.junit.Assert;

import kellerstrass.exposure.ExposureMaschine;
import kellerstrass.interestrate.models.StoredModels;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveInterpolation;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.curves.CurveInterpolation.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationEntity;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationMethod;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.Swap;
import net.finmath.montecarlo.interestrate.products.SwapLeg;
import net.finmath.montecarlo.interestrate.products.TermStructureMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.components.Notional;
import net.finmath.montecarlo.interestrate.products.indices.AbstractIndex;
import net.finmath.montecarlo.interestrate.products.indices.LIBORIndex;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.Schedule;
import net.finmath.time.SchedulePrototype;
import net.finmath.time.TimeDiscretizationFromArray;
import net.finmath.time.ScheduleGenerator.DaycountConvention;
import net.finmath.time.ScheduleGenerator.Frequency;
import net.finmath.time.ScheduleGenerator.ShortPeriodConvention;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;

/**
 *In this class we want to compare two different LiborSimulationModels
 * @author lucak
 *
 */
public class ExposureComparismOld {
	
	private final static int numberOfPaths		= 20000;
	
	private static DecimalFormat formatterMaturity	= new DecimalFormat("00.00", new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterValue		= new DecimalFormat(" ##0.000%;-##0.000%", new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation	= new DecimalFormat(" 0.00000E00;-0.00000E00", new DecimalFormatSymbols(Locale.ENGLISH));
	private final static NumberFormat formatter6 = new DecimalFormat("0.000000", new DecimalFormatSymbols(new Locale("en")));

	
	private static LIBORModelMonteCarloSimulationModel SimulationModel1;
	private static LIBORModelMonteCarloSimulationModel SimulationModel2;
	private static AbstractLIBORMonteCarloProduct swap1;
	private static AbstractLIBORMonteCarloProduct swap2;
	
	
	private final static LocalDate referenceDate = LocalDate.of(2017, 6, 15);

	
	
	public static void main(String[] args) throws Exception {
		
		initModels();
		
		// The two should be the same, since the tow models have to be initiated using the same forward curve!
		swap1 = initSwap(SimulationModel1);
		swap2 = initSwap(SimulationModel2);
		
		TermStructureMonteCarloProduct swapExposureEstimator1 = new ExposureMaschine(swap1);
		TermStructureMonteCarloProduct swapExposureEstimator2 = new ExposureMaschine(swap2);
		
		int numberOfExamplePaths = 10;
		//printExposurepaths(swapExposureEstimator1, SimulationModel1, numberOfExamplePaths);
		//printExposurepaths(swapExposureEstimator2, SimulationModel2, numberOfExamplePaths);
		
		printExpectedExposures(swapExposureEstimator1, swap1, SimulationModel1, numberOfExamplePaths);
		printExpectedExposures(swapExposureEstimator2, swap2, SimulationModel2, numberOfExamplePaths);
		
		
	}




private static void printExpectedExposures(TermStructureMonteCarloProduct swapExposureEstimator,
			AbstractLIBORMonteCarloProduct swap, LIBORModelMonteCarloSimulationModel simulationModel,
			int numberOfExamplePaths) throws CalculationException {
	System.out.println("We print exposure paths for:  " + simulationModel.getModel().getClass().getName()); 
	
	
	System.out.println("observationDate  \t   EEE  \t EPE  \t   ENE");
	for(double observationDate : simulationModel.getTimeDiscretization()) {

		if(observationDate == 0) {
			continue;
		}

		/*
		 * Calculate expected positive exposure of a swap
		 */
		RandomVariable valuesSwap = swap.getValue(observationDate, simulationModel);
		RandomVariable valuesEstimatedExposure = swapExposureEstimator.getValue(observationDate, simulationModel);
		RandomVariable valuesPositiveExposure = valuesSwap.mult(valuesEstimatedExposure.choose(new RandomVariableFromDoubleArray(1.0), new RandomVariableFromDoubleArray(0.0)));
		RandomVariable valuesNegativeExposure = valuesSwap.mult(valuesEstimatedExposure.choose(new RandomVariableFromDoubleArray(0.0), new RandomVariableFromDoubleArray(1.0)));
		
		double expectedEstimatiedExposure   = valuesEstimatedExposure.getAverage();
		double expectedPositiveExposure		= valuesPositiveExposure.getAverage();
		double expectedNegativeExposure		= valuesNegativeExposure.getAverage();

		System.out.println(observationDate + "    \t         " +  formatter6.format(expectedEstimatiedExposure) + "    \t         " +  formatter6.format(expectedPositiveExposure) + "    \t         " +  formatter6.format(expectedNegativeExposure) );


	}
	
}





private static void printExposurepaths(TermStructureMonteCarloProduct swapExposureEstimator,
			LIBORModelMonteCarloSimulationModel simulationModel, int numberOfExampleExposurePaths) throws CalculationException {
	System.out.println("We print exposure paths for:  " + simulationModel.getModel().getClass().getName()); 
	
	// store exposure  exposure paths             		
	double[][] exposurePaths  = new double[numberOfExampleExposurePaths][simulationModel.getTimeDiscretization().getNumberOfTimes()];
	  
	for(double observationDate : simulationModel.getTimeDiscretization()) {
                    int timeIndex = simulationModel.getTimeDiscretization().getTimeIndex(observationDate);
                    
					if(observationDate == 0) {
						continue;
					}
					/*
					 * Calculate the estimated exposure
					 */
					RandomVariable valuesEstimatedExposure = swapExposureEstimator.getValue(observationDate, simulationModel);
					
					for(int path = 0; path < numberOfExampleExposurePaths; path ++) {
					double exposureOnPath				= valuesEstimatedExposure.get(path);

					exposurePaths[path][timeIndex]= exposureOnPath;

				}	
		}
	
	//Print exposure paths
	System.out.print("observationDate  \t  ");
	for(int path = 0; path < exposurePaths.length ; path++ ) {
		System.out.print("path("+ path + ") \t  ");					
	}System.out.println("");
	
	
	
		for(double observationDate : simulationModel.getTimeDiscretization()) {
            int timeIndex = simulationModel.getTimeDiscretization().getTimeIndex(observationDate);
            
			if(observationDate == 0) {
				continue;
			}
			System.out.print(observationDate + " \t  ");
			
			for(int path = 0; path < exposurePaths.length ; path++ ) {
									System.out.print(formatter6.format(exposurePaths[path][timeIndex]) + "\t");
		}
		System.out.println("");		
	}
		
	}




/**
 * Initiation of the swap
 * @param simulationModel
 * @return 
 */
	private static AbstractLIBORMonteCarloProduct initSwap(LIBORModelMonteCarloSimulationModel simulationModel) {
		/*
		 * Set up the derivative
		 */
		SchedulePrototype fixMetaSchedule = new SchedulePrototype(
				Frequency.ANNUAL,
				DaycountConvention.E30_360,
				ShortPeriodConvention.FIRST,
				BusinessdayCalendar.DateRollConvention.MODIFIED_FOLLOWING,
				new BusinessdayCalendarExcludingTARGETHolidays(),
				0,
				0,
				false);

		SchedulePrototype floatMetaSchedule = new SchedulePrototype(
				Frequency.QUARTERLY,        
				DaycountConvention.ACT_360,
				ShortPeriodConvention.FIRST,
				BusinessdayCalendar.DateRollConvention.MODIFIED_FOLLOWING,
				new BusinessdayCalendarExcludingTARGETHolidays(),
				0,
				0,
				false);

		LocalDate startDate = referenceDate.plusMonths(1);
		LocalDate endDate = startDate.plusYears(15);
		
		Schedule fixLegSchedule = fixMetaSchedule.generateSchedule(referenceDate, startDate, endDate);
		Schedule floatLegSchedule = floatMetaSchedule.generateSchedule(referenceDate, startDate, endDate);
       
double rate = 0.035;
		
		//Create libor index for Monte-Carlo swap
        ForwardCurve forwardCurve = simulationModel.getModel().getForwardRateCurve();
		AbstractIndex libor = new LIBORIndex(forwardCurve.getName(), "EUR", "3M", floatMetaSchedule.getBusinessdayCalendar(), floatMetaSchedule.getDateRollConvention());

		//Create legs
		TermStructureMonteCarloProduct floatLeg = new SwapLeg(floatLegSchedule, new Notional(1.0), libor, 0, false);
		TermStructureMonteCarloProduct fixLeg = new SwapLeg(fixLegSchedule, new Notional(1.0), null, rate, false);
		
		
		return new Swap(floatLeg, fixLeg);
		
	}


/**
 * Initiation of the two models
 * @throws Exception 
 */
	private static void initModels() throws Exception {
		// Create the discount curve
				DiscountCurve discountCurve = DiscountCurveInterpolation.createDiscountCurveFromZeroRates(
						"discount curve",
						referenceDate,
						new double[] {0.5, 40.00}	/* zero rate end points */,
						new double[] {0.03,  0.04}	/* zeros */,
						new boolean[] {false,  false},
						InterpolationMethod.LINEAR,
						ExtrapolationMethod.CONSTANT,
						InterpolationEntity.LOG_OF_VALUE_PER_TIME
						);
				
				/*
				 * Create the libor tenor structure and the initial values
				 */
				double liborPeriodLength	= 0.25;
				double liborRateTimeHorzion	= 20.0;
				TimeDiscretizationFromArray liborPeriodDiscretization = new TimeDiscretizationFromArray(0.0, (int) (liborRateTimeHorzion / liborPeriodLength), liborPeriodLength);

				/*
				 * Create a simulation time discretization
				 */
				double lastTime	= 20.0;
				double dt		= 0.125;
				TimeDiscretizationFromArray simulationDiscretization = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);
				
				// Initialization:
				StoredModels modelStore = new StoredModels(liborPeriodDiscretization, simulationDiscretization, discountCurve, "3M", referenceDate);
				
				double shortRateVolatility = 0.005;
				double shortRateMeanreversion = 0.1;
				SimulationModel1 = modelStore.getLiborHullWhiteModellCorrespondingToLMM(numberOfPaths, shortRateVolatility, shortRateMeanreversion);
				SimulationModel2 = modelStore.getLiborMarketModellCorrespondingToHullWhite(numberOfPaths, shortRateVolatility, shortRateMeanreversion);
			
		
	}

}
